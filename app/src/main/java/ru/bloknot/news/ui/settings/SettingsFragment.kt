package ru.bloknot.news.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ru.bloknot.news.NewsApplication
import ru.bloknot.news.R
import ru.bloknot.news.adapters.SettingsAdapter
import ru.bloknot.news.databinding.FragmentNotificationsBinding
import ru.bloknot.news.models.SettingsItem

class SettingsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val settingsList = mutableListOf<SettingsItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSettingsList()

        val context: Context = binding.root.context

        val adapter = SettingsAdapter(settingsList) { position, checked ->
            when (position) {
                0 -> {
                    toggleNotifications(checked,context)
                    // Обновляем модель
                    settingsList[position] = settingsList[position].copy(switchChecked = checked)
                    // Уведомляем адаптер — теперь безопасно, потому что adapter уже создан!
                    //binding.settingsRecycler.adapter?.notifyItemChanged(position)
                }
            }
        }

        binding.settingsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter  // привязываем
        }
    }

    private fun setupSettingsList() {

        val prefs = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true) // по умолчанию включено

        settingsList.addAll(listOf(
            SettingsItem(R.drawable.ic_notifications_black_24dp, "Уведомления о новых новостях", "Только свежие",
                hasSwitch = true, switchChecked = notificationsEnabled),
        ))
    }

    private fun toggleNotifications(enabled: Boolean, context: Context) {

        if (enabled) {
            NewsApplication.startNewsBackgroundUpdate(context)  // ← включаем

        } else {
            NewsApplication.stopNewsBackgroundUpdate(context)    // ← выключаем
        }

        // Сохраняем состояние в SharedPreferences (чтобы после перезапуска приложения чекбокс остался в нужном положении)
        val prefs = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        prefs.edit { putBoolean("notifications_enabled", enabled) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
