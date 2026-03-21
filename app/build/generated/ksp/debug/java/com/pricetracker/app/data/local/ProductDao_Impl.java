package com.pricetracker.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.pricetracker.app.data.model.Product;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ProductDao_Impl implements ProductDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Product> __insertionAdapterOfProduct;

  private final EntityDeletionOrUpdateAdapter<Product> __deletionAdapterOfProduct;

  private final EntityDeletionOrUpdateAdapter<Product> __updateAdapterOfProduct;

  private final SharedSQLiteStatement __preparedStmtOfDeleteProductById;

  public ProductDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProduct = new EntityInsertionAdapter<Product>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `products` (`id`,`url`,`name`,`description`,`imageUrl`,`currentPrice`,`lowestPrice`,`highestPrice`,`store`,`currency`,`addedAt`,`lastCheckedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Product entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUrl());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getDescription());
        statement.bindString(5, entity.getImageUrl());
        statement.bindDouble(6, entity.getCurrentPrice());
        statement.bindDouble(7, entity.getLowestPrice());
        statement.bindDouble(8, entity.getHighestPrice());
        statement.bindString(9, entity.getStore());
        statement.bindString(10, entity.getCurrency());
        statement.bindLong(11, entity.getAddedAt());
        statement.bindLong(12, entity.getLastCheckedAt());
      }
    };
    this.__deletionAdapterOfProduct = new EntityDeletionOrUpdateAdapter<Product>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `products` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Product entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfProduct = new EntityDeletionOrUpdateAdapter<Product>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `products` SET `id` = ?,`url` = ?,`name` = ?,`description` = ?,`imageUrl` = ?,`currentPrice` = ?,`lowestPrice` = ?,`highestPrice` = ?,`store` = ?,`currency` = ?,`addedAt` = ?,`lastCheckedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Product entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUrl());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getDescription());
        statement.bindString(5, entity.getImageUrl());
        statement.bindDouble(6, entity.getCurrentPrice());
        statement.bindDouble(7, entity.getLowestPrice());
        statement.bindDouble(8, entity.getHighestPrice());
        statement.bindString(9, entity.getStore());
        statement.bindString(10, entity.getCurrency());
        statement.bindLong(11, entity.getAddedAt());
        statement.bindLong(12, entity.getLastCheckedAt());
        statement.bindLong(13, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteProductById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM products WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertProduct(final Product product, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfProduct.insertAndReturnId(product);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProduct(final Product product, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfProduct.handle(product);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProduct(final Product product, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfProduct.handle(product);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProductById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteProductById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteProductById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Product>> getAllProducts() {
    final String _sql = "SELECT * FROM products ORDER BY addedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"products"}, new Callable<List<Product>>() {
      @Override
      @NonNull
      public List<Product> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfCurrentPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPrice");
          final int _cursorIndexOfLowestPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "lowestPrice");
          final int _cursorIndexOfHighestPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "highestPrice");
          final int _cursorIndexOfStore = CursorUtil.getColumnIndexOrThrow(_cursor, "store");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final int _cursorIndexOfLastCheckedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastCheckedAt");
          final List<Product> _result = new ArrayList<Product>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Product _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpImageUrl;
            _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            final double _tmpCurrentPrice;
            _tmpCurrentPrice = _cursor.getDouble(_cursorIndexOfCurrentPrice);
            final double _tmpLowestPrice;
            _tmpLowestPrice = _cursor.getDouble(_cursorIndexOfLowestPrice);
            final double _tmpHighestPrice;
            _tmpHighestPrice = _cursor.getDouble(_cursorIndexOfHighestPrice);
            final String _tmpStore;
            _tmpStore = _cursor.getString(_cursorIndexOfStore);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            final long _tmpLastCheckedAt;
            _tmpLastCheckedAt = _cursor.getLong(_cursorIndexOfLastCheckedAt);
            _item = new Product(_tmpId,_tmpUrl,_tmpName,_tmpDescription,_tmpImageUrl,_tmpCurrentPrice,_tmpLowestPrice,_tmpHighestPrice,_tmpStore,_tmpCurrency,_tmpAddedAt,_tmpLastCheckedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getProductById(final long id, final Continuation<? super Product> $completion) {
    final String _sql = "SELECT * FROM products WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Product>() {
      @Override
      @Nullable
      public Product call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfCurrentPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPrice");
          final int _cursorIndexOfLowestPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "lowestPrice");
          final int _cursorIndexOfHighestPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "highestPrice");
          final int _cursorIndexOfStore = CursorUtil.getColumnIndexOrThrow(_cursor, "store");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final int _cursorIndexOfLastCheckedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastCheckedAt");
          final Product _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpImageUrl;
            _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            final double _tmpCurrentPrice;
            _tmpCurrentPrice = _cursor.getDouble(_cursorIndexOfCurrentPrice);
            final double _tmpLowestPrice;
            _tmpLowestPrice = _cursor.getDouble(_cursorIndexOfLowestPrice);
            final double _tmpHighestPrice;
            _tmpHighestPrice = _cursor.getDouble(_cursorIndexOfHighestPrice);
            final String _tmpStore;
            _tmpStore = _cursor.getString(_cursorIndexOfStore);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            final long _tmpLastCheckedAt;
            _tmpLastCheckedAt = _cursor.getLong(_cursorIndexOfLastCheckedAt);
            _result = new Product(_tmpId,_tmpUrl,_tmpName,_tmpDescription,_tmpImageUrl,_tmpCurrentPrice,_tmpLowestPrice,_tmpHighestPrice,_tmpStore,_tmpCurrency,_tmpAddedAt,_tmpLastCheckedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllProductsList(final Continuation<? super List<Product>> $completion) {
    final String _sql = "SELECT * FROM products";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Product>>() {
      @Override
      @NonNull
      public List<Product> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfCurrentPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPrice");
          final int _cursorIndexOfLowestPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "lowestPrice");
          final int _cursorIndexOfHighestPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "highestPrice");
          final int _cursorIndexOfStore = CursorUtil.getColumnIndexOrThrow(_cursor, "store");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final int _cursorIndexOfLastCheckedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastCheckedAt");
          final List<Product> _result = new ArrayList<Product>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Product _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpImageUrl;
            _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            final double _tmpCurrentPrice;
            _tmpCurrentPrice = _cursor.getDouble(_cursorIndexOfCurrentPrice);
            final double _tmpLowestPrice;
            _tmpLowestPrice = _cursor.getDouble(_cursorIndexOfLowestPrice);
            final double _tmpHighestPrice;
            _tmpHighestPrice = _cursor.getDouble(_cursorIndexOfHighestPrice);
            final String _tmpStore;
            _tmpStore = _cursor.getString(_cursorIndexOfStore);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            final long _tmpLastCheckedAt;
            _tmpLastCheckedAt = _cursor.getLong(_cursorIndexOfLastCheckedAt);
            _item = new Product(_tmpId,_tmpUrl,_tmpName,_tmpDescription,_tmpImageUrl,_tmpCurrentPrice,_tmpLowestPrice,_tmpHighestPrice,_tmpStore,_tmpCurrency,_tmpAddedAt,_tmpLastCheckedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
