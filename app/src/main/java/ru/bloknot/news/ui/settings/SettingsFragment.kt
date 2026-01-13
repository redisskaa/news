package ru.bloknot.news.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ru.bloknot.news.R
import ru.bloknot.news.adapters.SettingsAdapter
import ru.bloknot.news.databinding.FragmentNotificationsBinding
import ru.bloknot.news.models.SettingsItem
import ru.bloknot.news.worker.NewsUpdateWorker
import java.util.concurrent.TimeUnit

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

        val adapter = SettingsAdapter(settingsList) { position, checked ->
            when (position) {
                0 -> {
                    toggleNotifications(checked)
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

    private fun setupPeriodicNewsUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)   // Только при интернете
            .setRequiresBatteryNotLow(true)                  // Не при низком заряде
            .build()

        val workRequest = PeriodicWorkRequestBuilder<NewsUpdateWorker>(
            repeatInterval = 1L,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("news_update")
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "news_background_update",           // уникальное имя
            ExistingPeriodicWorkPolicy.KEEP,     // если уже запущено — не дублируем
            workRequest
        )
    }

    private fun setupSettingsList() {

        val prefs = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true) // по умолчанию включено

        settingsList.addAll(listOf(
            SettingsItem(R.drawable.ic_notifications_black_24dp, "Уведомления о новых новостях", "Только свежие",
                hasSwitch = true, switchChecked = notificationsEnabled),
            SettingsItem(R.drawable.ic_home_black_24dp, "Тёмная тема", "Следовать системе"),
            SettingsItem(R.drawable.ic_dashboard_black_24dp, "О приложении", "Версия 1.0"),
            SettingsItem(R.drawable.ic_launcher_background, "Поделиться приложением"),
            SettingsItem(R.drawable.ic_no_connection, "Оценить в Google Play"),
            SettingsItem(R.drawable.ic_no_connection, "Оценить в Google Play"),
            SettingsItem(R.drawable.ic_no_connection, "Оценить в Google Play"),
            SettingsItem(R.drawable.ic_no_connection, "Оценить в Google Play"),
            SettingsItem(R.drawable.ic_no_connection, "Оценить в Google Play"),
            SettingsItem(R.drawable.ic_no_connection, "Оценить в Google Play"),

        ))
    }

    private fun toggleNotifications(enabled: Boolean) {
        val workManager = WorkManager.getInstance(requireContext())

        if (enabled) {
            setupPeriodicNewsUpdate()// вызовем тот же метод из NewsApplication
        } else {
            // Отключаем все уведомления о новостях
            workManager.cancelUniqueWork("news_background_update")
            workManager.cancelUniqueWork("test_news_notification_once") // если использовал тест
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
