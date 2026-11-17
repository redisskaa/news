// File: app/src/main/java/ru/bloknot/news/ui/dashboard/DashboardFragment.kt
package ru.bloknot.news.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
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
        val data = ArrayList<String>()
        listUrl = arrayListOf()

        try {
            for (b in result.indices) {
                data.add(result[b].text())
            }

            for (c in result.indices) {
                listUrl.add(result[c].attr("abs:href"))
            }

            // ⚠️ Жёсткие индексы — сохраняем поведение оригинала, но с защитой от краша
            if (listUrl.size > 10) listUrl[10] = "https://bloknot-krasnodar.ru/news/a_request_to_the_editor/"
            if (listUrl.size > 7) listUrl[7] = "https://bloknot-krasnodar.ru/news/officials_of_the_city/"

            // Удаляем по убыванию индексов, чтобы не смещались позиции
            listOf(16, 14, 3, 2).forEach { idx ->
                if (idx < listUrl.size) listUrl.removeAt(idx)
            }

            WordRemover.removeItemsContainingWords(data, getString(R.string.wordsRemove))
        } catch (e: IndexOutOfBoundsException) {
            Log.e("DashboardFragment", "IndexOutOfBounds: ${e.message}")
        }

        val adapter = ListViewAdapter(requireContext(), data)
        adapter.notifyDataSetChanged()
        binding.myListView.adapter = adapter
        progressBar.visibility = View.GONE
    }

    override fun onProgressUpdate(percent: Int) {
        progressBar.progress = percent
    }

    override fun onError(e: Exception) {
        Log.e("DashboardFragment", "onError", e)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    object WordRemover {

        fun removeItemsContainingWords(list: ArrayList<String>, wordsToRemove: String) {
            val wordSet = parseWords(wordsToRemove)
            if (wordSet.isEmpty()) return
            list.removeAll { item -> containsAnyWord(item, wordSet) }
        }

        private fun parseWords(wordsStr: String?): Set<String> {
            if (wordsStr.isNullOrBlank()) return emptySet()
            return wordsStr.split(",")
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                .toSet()
        }

        private fun containsAnyWord(item: String, words: Set<String>): Boolean {
            val lower = item.lowercase()
            return words.any { lower.contains(it) }
        }
    }
}