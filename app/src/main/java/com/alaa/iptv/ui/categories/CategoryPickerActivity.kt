package com.alaa.iptv.ui.categories

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.databinding.ActivityCategoryPickerBinding

class CategoryPickerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryPickerBinding
    private lateinit var adapter: CategoryPickerAdapter
    private var categories: List<Category> = emptyList()
    private lateinit var prefs: AppPreferences
    private lateinit var pickerTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        com.alaa.iptv.ui.common.PremiumNeonFocus.install(binding.root)

        prefs = AppPreferences(this)
        categories = intent.parcelableCategories()
        pickerTitle = intent.getStringExtra(EXTRA_TITLE) ?: "الفئات"
        binding.titleText.text = pickerTitle
        binding.backButton.setOnClickListener { finish() }

        adapter = CategoryPickerAdapter(::returnSelection, ::hideCategory)
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.categoriesRecyclerView.adapter = adapter
        binding.searchInput.doAfterTextChanged { showFiltered(it?.toString().orEmpty()) }
        showFiltered("")
    }

    private fun showFiltered(query: String) {
        val visible = if (prefs.isFeatureEnabled(FeatureCatalog.HIDE_CONTENT)) {
            categories.filterNot { prefs.isContentHidden(categoryKey(it)) }
        } else categories
        val filtered = CategorySearch.filter(visible, query)
        binding.categoryCount.text = if (query.isBlank()) {
            "${filtered.size} فئة متاحة"
        } else {
            "${filtered.size} نتيجة بحث"
        }
        adapter.submit(filtered)
        binding.emptyText.visibility = if (filtered.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun returnSelection(category: Category) {
        setResult(
            Activity.RESULT_OK,
            Intent()
                .putExtra(EXTRA_CATEGORY_ID, category.categoryId)
                .putExtra(EXTRA_CATEGORY_NAME, category.categoryName)
        )
        finish()
    }

    private fun hideCategory(category: Category) {
        if (!prefs.isFeatureEnabled(FeatureCatalog.HIDE_CONTENT)) {
            Toast.makeText(this, "فعّل إخفاء المحتوى من الإعدادات أولاً", Toast.LENGTH_SHORT).show()
            return
        }
        prefs.setContentHidden(categoryKey(category), true)
        Toast.makeText(this, "أُخفيت ${category.categoryName}. يمكنك استعادة كل الفئات من إدارة الحسابات.", Toast.LENGTH_LONG).show()
        showFiltered(binding.searchInput.text?.toString().orEmpty())
    }

    private fun categoryKey(category: Category): String = "$pickerTitle:${category.categoryId}"

    @Suppress("DEPRECATION")
    private fun Intent.parcelableCategories(): List<Category> =
        getParcelableArrayListExtra<Category>(EXTRA_CATEGORIES)?.toList().orEmpty()

    companion object {
        const val EXTRA_TITLE = "CATEGORY_PICKER_TITLE"
        const val EXTRA_CATEGORIES = "CATEGORY_PICKER_CATEGORIES"
        const val EXTRA_CATEGORY_ID = "CATEGORY_PICKER_RESULT_ID"
        const val EXTRA_CATEGORY_NAME = "CATEGORY_PICKER_RESULT_NAME"

        fun createIntent(context: Context, title: String, categories: List<Category>): Intent =
            Intent(context, CategoryPickerActivity::class.java)
                .putExtra(EXTRA_TITLE, title)
                .putParcelableArrayListExtra(EXTRA_CATEGORIES, ArrayList(categories))
    }
}
