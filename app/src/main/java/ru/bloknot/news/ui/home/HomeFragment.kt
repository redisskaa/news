package ru.bloknot.news.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import ru.bloknot.news.adapters.CustomAdapter
import ru.bloknot.news.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: CustomAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Starting", "Стартуем HomeFragment")
        setupRecyclerView()
        observeUiState()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadNews()
        }
    }

    private fun setupRecyclerView() {
        adapter = CustomAdapter()
        binding.recyclerHome.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is HomeUiState.Loading -> {
                        binding.swipeRefresh.isRefreshing = true
                    }

                    is HomeUiState.Success -> {
                        binding.progBarMain.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        adapter.updateNews(state.news)
                    }

                    is HomeUiState.Error -> {
                        binding.progBarMain.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }

                    else -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}