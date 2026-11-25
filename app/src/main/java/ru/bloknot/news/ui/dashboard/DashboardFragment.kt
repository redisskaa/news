package ru.bloknot.news.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import ru.bloknot.news.R
import ru.bloknot.news.activity.MainActivity
import ru.bloknot.news.adapters.ListViewAdapter
import ru.bloknot.news.databinding.FragmentDashboardBinding
import ru.bloknot.news.helpclass.CachedCategory
import ru.bloknot.news.helpclass.CategoryCache
import ru.bloknot.news.interfaces.JsoupParseCallback
import ru.bloknot.news.internet.JsoupTask
import ru.bloknot.news.utils.Constants

class DashboardFragment : Fragment(), JsoupParseCallback {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var progressBar: ProgressBar
    private var listUrl: ArrayList<String> = arrayListOf()
    private var hasCachedData = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Стартуем DashboardFragment")

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root = binding.root
        val context = requireContext()

        progressBar = root.findViewById(R.id.progBar)

        binding.myListView.setOnItemClickListener { parent, _, position, id ->
            val itemValue = parent.getItemAtPosition(position).toString()
            val pos = listUrl[position]
            val posi = listUrl.indexOf(pos)

            if (position == posi) {
                val intent = Intent(root.context, MainActivity::class.java)
                intent.putExtra("cat", itemValue)
                intent.putExtra("BASE_URL", Constants.BASE_URL)
                intent.putStringArrayListExtra("list_url", listUrl)
                intent.putExtra("position", posi)
                startActivity(intent)
                Log.d("ListViewClick", "Position: $posi, ID: $id, Value: $itemValue")
            }
        }

        val cache = CategoryCache.load(context)
        when {
            cache != null && CategoryCache.isFresh(cache.timestamp) -> {
                Log.d(TAG, "Свежий кэш (${cache.items.size}), без сети")
                hasCachedData = true
                showCategories(cache.items)
            }
            cache != null -> {
                Log.d(TAG, "Устаревший кэш (${cache.items.size}), фоновая проверка")
                hasCachedData = true
                showCategories(cache.items)
                JsoupTask(this, context).execute(Constants.BASE_URL, "a.link_nav_second")
            }
            else -> {
                Log.d(TAG, "Кэша нет, полный парсинг")
                JsoupTask(this, context).execute(Constants.BASE_URL, "a.link_nav_second")
            }
        }

        return root
    }

    override fun onPreExecute() {
        if (!hasCachedData && _binding != null) {
            progressBar.visibility = View.VISIBLE
        }
    }

    override fun onPostExecute(result: Elements) {
        val rawItems = result.map { el -> el.text() to el.attr("abs:href") }
        val filtered = applyFilters(rawItems)

        if (hasCachedData) {
            // Кэш уже показан, обновляем в фоне
            refreshInBackground(filtered)
        } else {
            // Первый запуск — полный парсинг с прогресс-баром
            fullParseAndShow(filtered)
        }
    }

    private fun applyFilters(raw: List<Pair<String, String>>): List<Pair<String, String>> {
        val words = getString(R.string.wordsRemove)
            .split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }

        return raw.filter { (title, url) ->
            val lower = title.lowercase()
            words.none { lower.contains(it) } && !url.contains("/health/")
        }
    }

    private fun showCategories(items: List<CachedCategory>) {
        val data = ArrayList(items.map { it.title })
        listUrl = ArrayList(items.map { it.url })
        val adapter = ListViewAdapter(requireContext(), data)
        binding.myListView.adapter = adapter
    }

    private fun fullParseAndShow(items: List<Pair<String, String>>) {
        val context = requireContext()
        lifecycleScope.launch {
            val valid = withContext(Dispatchers.IO) {
                items.filter { (_, url) -> hasContent(context, url) }
                    .map { CachedCategory(it.first, it.second) }
            }
            if (_binding == null) return@launch

            if (valid.isEmpty()) {
                Log.w(TAG, "Все категории пусты — показываем как есть")
                // Если hasContent завалил всё — показываем хотя бы названия,
                // иначе пользователь увидит пустой экран без объяснений
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Не удалось загрузить категории", Toast.LENGTH_SHORT).show()
                return@launch
            }

            CategoryCache.save(requireContext(), valid)
            showCategories(valid)
            progressBar.visibility = View.GONE
        }
    }

    private fun refreshInBackground(items: List<Pair<String, String>>) {
        val context = requireContext()
        val currentUrls = listUrl.toList()

        lifecycleScope.launch {
            val valid = withContext(Dispatchers.IO) {
                items.filter { (_, url) -> hasContent(context, url) }
                    .map { CachedCategory(it.first, it.second) }
            }
            if (_binding == null) return@launch
            if (valid.isEmpty()) return@launch

            CategoryCache.save(requireContext(), valid)

            val newUrls = valid.map { it.url }
            if (newUrls != currentUrls) {
                Log.d(TAG, "Список изменился (${currentUrls.size} → ${newUrls.size}), обновляем UI")
                showCategories(valid)
            } else {
                Log.d(TAG, "Список не изменился")
            }
        }
    }

    private suspend fun hasContent(context: Context, url: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val doc = Jsoup.connect(url)
                    .userAgent(context.getString(R.string.userAgentMobile))
                    .get()
                doc.select("ul.bigline>li").isNotEmpty() ||
                        doc.select("div.catitem-row>div.catitem").isNotEmpty() ||
                        doc.select("article").isNotEmpty() ||
                        doc.select(".last__item>a").isNotEmpty()
            } catch (e: Exception) {
                Log.e(TAG, "hasContent error: $url", e)
                false
            }
        }

    override fun onProgressUpdate(percent: Int) {
        if (_binding != null) progressBar.progress = percent
    }

    override fun onError(e: Exception) {
        Log.e(TAG, "onError", e)
        if (_binding == null) return
        progressBar.visibility = View.GONE
        if (!hasCachedData) {
            Toast.makeText(requireContext(), "Не удалось загрузить категории", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "DashboardFragment"
    }
}