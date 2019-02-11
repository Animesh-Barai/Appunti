package it.sephiroth.android.app.appunti.db

import com.dbflow5.config.FlowManager
import com.dbflow5.query.*
import com.dbflow5.structure.save
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import it.sephiroth.android.app.appunti.db.tables.Category
import it.sephiroth.android.app.appunti.db.tables.Category_Table
import it.sephiroth.android.app.appunti.db.tables.Entry
import it.sephiroth.android.app.appunti.db.tables.Entry_Table
import it.sephiroth.android.app.appunti.ext.rxSingle
import timber.log.Timber
import java.util.*

object DatabaseHelper {


    fun getCategories(): Single<MutableList<Category>> {
        return rxSingle(Schedulers.io()) {
            select().from(Category::class).orderBy(OrderBy(Category_Table.categoryID.nameAlias, true)).list
        }
    }

    fun getCategoryByID(id: Int): Category? {
        return select().from(Category::class).where(Category_Table.categoryID.eq(id)).result
    }

    fun setEntryPinned(entry: Entry, value: Boolean): Boolean {
        entry.entryPinned = if (value) 1 else 0
        entry.entryModifiedDate = Date()
        return entry.save()
    }

    fun setEntryArchived(entry: Entry, value: Boolean): Boolean {
        entry.entryArchived = if (value) 1 else 0
        entry.entryModifiedDate = Date()
        if (value) entry.entryDeleted = 0
        return entry.save()
    }

    fun setEntryDeleted(entry: Entry, value: Boolean): Boolean {
        entry.entryDeleted = if (value) 1 else 0
        entry.entryModifiedDate = Date()
        if (value) entry.entryArchived = 0
        return entry.save()
    }

    fun setEntryCategory(entry: Entry, category: Category?): Boolean {
        entry.category = category
        entry.entryModifiedDate = Date()
        return entry.save()
    }

    fun setEntriesPinned(values: List<Entry>, pin: Boolean): Single<Unit> {
        val pinnedValue = if (pin) 1 else 0

        return rxSingle(Schedulers.io()) {
            update(Entry::class)
                .set(Entry_Table.entryPinned.eq(pinnedValue), Entry_Table.entryModifiedDate.eq(Date()))
                .where(Entry_Table.entryID.`in`(values.map { it.entryID }))
                .execute(FlowManager.getDatabase(AppDatabase::class.java))
        }
    }

    fun setEntriesDeleted(values: List<Entry>, delete: Boolean): Single<Unit> {
        return rxSingle(Schedulers.io()) {
            update(Entry::class)
                .set(Entry_Table.entryDeleted.eq(if (delete) 1 else 0),
                        Entry_Table.entryArchived.eq(0),
                        Entry_Table.entryModifiedDate.eq(Date()))
                .where(Entry_Table.entryID.`in`(values.map { it.entryID }))
                .execute(FlowManager.getDatabase(AppDatabase::class.java))
        }
    }

    fun setEntriesArchived(values: List<Entry>, archive: Boolean): Single<Unit> {
        Timber.i("setEntriesArchived($archive, $values")
        return rxSingle(Schedulers.io()) {
            update(Entry::class)
                .set(Entry_Table.entryDeleted.eq(0),
                        Entry_Table.entryArchived.eq(if (archive) 1 else 0),
                        Entry_Table.entryModifiedDate.eq(Date()))
                .where(Entry_Table.entryID.`in`(values.map { it.entryID }))
                .execute(FlowManager.getDatabase(AppDatabase::class.java))

        }
    }

    fun getEntriesByCategory(category: Category?): Single<MutableList<Entry>> {
        return rxSingle(Schedulers.io()) {
            select().from(Entry::class)
                .run {
                    category?.let {
                        where(Entry_Table.category_categoryID.eq(it.categoryID))
                            .and(Entry_Table.entryArchived.eq(0))
                            .and(Entry_Table.entryDeleted.eq(0)) as Transformable<Entry>
                    } ?: run {
                        where(Entry_Table.entryArchived.eq(0)).and(Entry_Table.entryDeleted.eq(0)) as Transformable<Entry>
                    }

                }.run {
                    orderByAll(listOf(
                            OrderBy(Entry_Table.entryPinned.nameAlias, false),
                            OrderBy(Entry_Table.entryPriority.nameAlias, false),
                            OrderBy(Entry_Table.entryModifiedDate.nameAlias, false))).list
                }
        }
    }

    fun getEntryById(id: Int): Single<Entry?> {
        return rxSingle(Schedulers.io()) {
            select().from(Entry::class).where(Entry_Table.entryID.eq(id)).result
        }
    }

    fun getEntries(func: From<Entry>.() -> Transformable<Entry>): Single<MutableList<Entry>> {
        return rxSingle(Schedulers.io()) {
            select().from(Entry::class)
                .run {
                    this.func()
                }.run {
                    orderByAll(listOf(
                            OrderBy(Entry_Table.entryArchived.nameAlias, false),
                            OrderBy(Entry_Table.entryDeleted.nameAlias, false),
                            OrderBy(Entry_Table.entryPinned.nameAlias, false),
                            OrderBy(Entry_Table.entryPriority.nameAlias, false),
                            OrderBy(Entry_Table.entryModifiedDate.nameAlias, false))).list
                }
        }
    }
}