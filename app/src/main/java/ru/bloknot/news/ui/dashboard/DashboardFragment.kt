// File: app/src/main/java/ru/bloknot/news/ui/dashboard/DashboardFragment.kt
package ru.bloknot.news.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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
import ru.bloknot.news.interfaces.JsoupParseCallback
import ru.bloknot.news.internet.JsoupTask
import ru.bloknot.news.utils.Constants

class DashboardFragment : Fragment(), JsoupParseCallback {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var progressBar: ProgressBar
    private var listUrl: ArrayList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("Starting", "Стартуем DashboardFragment")

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root = binding.root
        val context = requireContext()

        progressBar = root.findViewById(R.id.progBar)

        JsoupTask(this, context).execute(Constants.BASE_URL, "a.link_nav_second")

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
            } else {
                Log.e("ListViewClick", "Position mismatch: $position != $posi")
            }
        }

        return root
    }

    override fun onPreExecute() {
        progressBar.visibility = View.VISIBLE
    }

    override fun onPostExecute(result: Elements) {
        // 1. Собираем пары (название, url) — они всегда синхронны
        val items = result.map { el -> el.text() to el.attr("abs:href") }.toMutableList()

        // 2. Фильтр по словам из ресурса
        val wordsToRemove = getString(R.string.wordsRemove)
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        items.removeAll { (title, _) ->
            wordsToRemove.any { title.lowercase().contains(it) }
        }

        // 3. Убираем health
        items.removeAll { (_, url) -> url.contains("/health/") }

        // 4. Проверяем каждую категорию на наличие контента
        val context = requireContext()
        lifecycleScope.launch {

            val validItems = withContext(Dispatchers.IO) {
                items.filter { (_, url) -> hasContent(context, url) }
            }
            if (_binding == null) return@launch

            val data = ArrayList(validItems.map { it.first })
            listUrl = ArrayList(validItems.map { it.second })

            Log.d("DashboardFragment", "Показано категорий: ${data.size}")
            validItems.forEach { Log.d("DashboardFragment", "  ${it.first} -> ${it.second}") }

            val adapter = ListViewAdapter(requireContext(), data)
            binding.myListView.adapter = adapter
            progressBar.visibility = View.GONE
        }
    }

    /**
     * Проверяет, есть ли на странице категории хоть один элемент новости.
     * Использует те же селекторы, что и ParseTask в MainActivity.
     */
    private suspend fun hasContent(context: Context, url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent(context.getString(R.string.userAgentMobile))
                .get()

            val hasBigline = doc.select("ul.bigline>li").isNotEmpty()
            val hasCatitem = doc.select("div.catitem-row>div.catitem").isNotEmpty()
            val hasArticle = doc.select("article").isNotEmpty()
            val hasLastItem = doc.select(".last__item>a").isNotEmpty()

            val result = hasBigline || hasCatitem || hasArticle || hasLastItem
            Log.d("DashboardFragment", "hasContent $url = $result (bigline=$hasBigline, catitem=$hasCatitem, article=$hasArticle, last=$hasLastItem)")
            result
        } catch (e: Exception) {
            Log.e("DashboardFragment", "hasContent error for $url", e)
            false
        }
    }

    override fun onProgressUpdate(percent: Int) {
        progressBar.progress = percent
    }

    override fun onError(e: Exception) {
        Log.e("DashboardFragment", "onError", e)
        progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}