/*
 * Copyright (C) 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eventplant.ep.eventplant1.history;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.eventplant.ep.eventplant1.Intents;
import com.eventplant.ep.eventplant1.PreferencesActivity;
import com.eventplant.ep.eventplant1.result.ResultHandler;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>Manages functionality related to scan history.</p>
 *
 * @author Sean Owen
 */
public final class HistoryManager {


  private static final String TAG = HistoryManager.class.getSimpleName();

  private static final int MAX_ITEMS = 2000;

  private static final String[] COLUMNS = {
      DBHelper.TEXT_COL,
      DBHelper.DISPLAY_COL,
      DBHelper.FORMAT_COL,
      DBHelper.TIMESTAMP_COL,
      DBHelper.DETAILS_COL,
  };

  private static final String[] COUNT_COLUMN = { "COUNT(1)" };

  private static final String[] ID_COL_PROJECTION = { DBHelper.ID_COL };
  private static final String[] ID_DETAIL_COL_PROJECTION = { DBHelper.ID_COL, DBHelper.DETAILS_COL };
  private static final Pattern DOUBLE_QUOTE = Pattern.compile("\"", Pattern.LITERAL);

  private final Activity activity;
  private final boolean enableHistory;

  public HistoryManager(Activity activity) {
    this.activity = activity;
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    enableHistory = prefs.getBoolean(PreferencesActivity.KEY_ENABLE_HISTORY, true);
  }

  public boolean hasHistoryItems() {
    SQLiteOpenHelper helper = new DBHelper(activity);
    try (SQLiteDatabase db = helper.getReadableDatabase();
         Cursor cursor = db.query(DBHelper.TABLE_NAME, COUNT_COLUMN, null, null, null, null, null)) {
      cursor.moveToFirst();
      return cursor.getInt(0) > 0;
    } catch (SQLException sqle) {
      Log.w(TAG, sqle);
      return false;
    }
  }

  public List<HistoryItem> buildHistoryItems() {
    SQLiteOpenHelper helper = new DBHelper(activity);
    List<HistoryItem> items = new ArrayList<>();
    try (SQLiteDatabase db = helper.getReadableDatabase();
         Cursor cursor = db.query(DBHelper.TABLE_NAME,
                                  COLUMNS,
                                  null, null, null, null,
                                  DBHelper.TIMESTAMP_COL + " DESC")) {
      while (cursor.moveToNext()) {
        String text = cursor.getString(0);
        String display = cursor.getString(1);
        String format = cursor.getString(2);
        long timestamp = cursor.getLong(3);
        String details = cursor.getString(4);
        Result result = new Result(text, null, null, BarcodeFormat.valueOf(format), timestamp);
        items.add(new HistoryItem(result, display, timestamp));
      }
    } catch (CursorIndexOutOfBoundsException cioobe) {
      Log.w(TAG, cioobe);
    }
    return items;
  }

  public HistoryItem buildHistoryItem(int number) {
    SQLiteOpenHelper helper = new DBHelper(activity);
    try (SQLiteDatabase db = helper.getReadableDatabase();
         Cursor cursor = db.query(DBHelper.TABLE_NAME,
                                  COLUMNS,
                                  null, null, null, null,
                                  DBHelper.TIMESTAMP_COL + " DESC")) {
      cursor.move(number + 1);
      String text = cursor.getString(0);
      String display = cursor.getString(1);
      String format = cursor.getString(2);
      long timestamp = cursor.getLong(3);
      String details = cursor.getString(4);
      Result result = new Result(text, null, null, BarcodeFormat.valueOf(format), timestamp);
      return new HistoryItem(result, display, timestamp);
    }
  }
  
  public void deleteHistoryItem(int number) {
    SQLiteOpenHelper helper = new DBHelper(activity);
    try (SQLiteDatabase db = helper.getWritableDatabase();
         Cursor cursor = db.query(DBHelper.TABLE_NAME,
                                  ID_COL_PROJECTION,
                                  null, null, null, null,
                                  DBHelper.TIMESTAMP_COL + " DESC")) {
      cursor.move(number + 1);
      db.delete(DBHelper.TABLE_NAME, DBHelper.ID_COL + '=' + cursor.getString(0), null);
    } catch (SQLException sqle) {
      Log.w(TAG, sqle);
    }
  }

  public void addHistoryItem(Result result, ResultHandler handler, String name) {
    // Do not save this item to the history if the preference is turned off, or the contents are
    // considered secure.

    if (!activity.getIntent().getBooleanExtra(Intents.Scan.SAVE_HISTORY, true) ||
        handler.areContentsSecure() || !enableHistory) {
      return;
    }

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    if (!prefs.getBoolean(PreferencesActivity.KEY_REMEMBER_DUPLICATES, false)) {
      deletePrevious(result.getText());
    }
    CharSequence displayContents = handler.getDisplayContents();
    String charstr = String.valueOf(displayContents);
    ContentValues values = new ContentValues();

    SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault());
    Date date = new Date();

    String strDate = dateFormat.format(date);

    values.put(DBHelper.TEXT_COL, result.getText()+"번 " +name+"님");
    values.put(DBHelper.FORMAT_COL, result.getBarcodeFormat().toString());
    values.put(DBHelper.DISPLAY_COL, handler.getDisplayContents().toString());
    values.put(DBHelper.TIMESTAMP_COL, strDate);

    SQLiteOpenHelper helper = new DBHelper(activity);
    try (SQLiteDatabase db = helper.getWritableDatabase()) {
      // Insert the new entry into the DB.
      db.insert(DBHelper.TABLE_NAME, DBHelper.TIMESTAMP_COL, values);
    }
  }

  public void addHistoryItemDetails(String itemID, String itemDetails) {
    // As we're going to do an update only we don't need need to worry
    // about the preferences; if the item wasn't saved it won't be udpated
    SQLiteOpenHelper helper = new DBHelper(activity);
    try (SQLiteDatabase db = helper.getWritableDatabase();
         Cursor cursor = db.query(DBHelper.TABLE_NAME,
                                  ID_DETAIL_COL_PROJECTION,
                                  DBHelper.TEXT_COL + "=?",
                                  new String[] { itemID },
                                  null,
                                  null,
                                  DBHelper.TIMESTAMP_COL + " DESC",
                                  "1")) {
      String oldID = null;
      String oldDetails = null;
      if (cursor.moveToNext()) {
        oldID = cursor.getString(0);
        oldDetails = cursor.getString(1);
      }

      if (oldID != null) {
        String newDetails;
        if (oldDetails == null) {
          newDetails = itemDetails;
        } else if (oldDetails.contains(itemDetails)) {
          newDetails = null;
        } else {
          newDetails = oldDetails + " : " + itemDetails;
        } 
        if (newDetails != null) {
          ContentValues values = new ContentValues();
          values.put(DBHelper.DETAILS_COL, newDetails);
          db.update(DBHelper.TABLE_NAME, values, DBHelper.ID_COL + "=?", new String[] { oldID });
        }
      }
    }
  }

  private void deletePrevious(String text) {
    SQLiteOpenHelper helper = new DBHelper(activity);
    try (SQLiteDatabase db = helper.getWritableDatabase()) {
      db.delete(DBHelper.TABLE_NAME, DBHelper.TEXT_COL + "=?", new String[] { text });
    } catch (SQLException sqle) {
      Log.w(TAG, sqle);
    }
  }

  public void trimHistory() {
    SQLiteOpenHelper helper = new DBHelper(activity);
    try (SQLiteDatabase db = helper.getWritableDatabase();
         Cursor cursor = db.query(DBHelper.TABLE_NAME,
                                  ID_COL_PROJECTION,
                                  null, null, null, null,
                                  DBHelper.TIMESTAMP_COL + " DESC")) {
      cursor.move(MAX_ITEMS);
      while (cursor.moveToNext()) {
        String id = cursor.getString(0);
        Log.i(TAG, "Deleting scan history ID " + id);
        db.delete(DBHelper.TABLE_NAME, DBHelper.ID_COL + '=' + id, null);
      }
    } catch (SQLException sqle) {
      Log.w(TAG, sqle);
    }
  }

  /**
   * <p>Builds a text representation of the scanning history. Each scan is encoded on one
   * line, terminated by a line break (\r\n). The values in each line are comma-separated,
   * and double-quoted. Double-quotes within values are escaped with a sequence of two
   * double-quotes. The fields output are:</p>
   *
   * <ol>
   *  <li>Raw text</li>
   *  <li>Display text</li>
   *  <li>Format (e.g. QR_CODE)</li>
   *  <li>Unix timestamp (milliseconds since the epoch)</li>
   *  <li>Formatted version of timestamp</li>
   *  <li>Supplemental info (e.g. price info for a product barcode)</li>
   * </ol>
   */
  CharSequence buildHistory() {
    SQLiteOpenHelper helper = new DBHelper(activity);
    try (SQLiteDatabase db = helper.getWritableDatabase();
         Cursor cursor = db.query(DBHelper.TABLE_NAME,
                                  COLUMNS,
                                  null, null, null, null,
                                  DBHelper.TIMESTAMP_COL + " DESC")) {
      DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
      StringBuilder historyText = new StringBuilder(1000);
      while (cursor.moveToNext()) {

        historyText.append('"').append(massageHistoryField(cursor.getString(0))).append("\",");
        historyText.append('"').append(massageHistoryField(cursor.getString(1))).append("\",");
        historyText.append('"').append(massageHistoryField(cursor.getString(2))).append("\",");
        historyText.append('"').append(massageHistoryField(cursor.getString(3))).append("\",");

        // Add timestamp again, formatted
        long timestamp = cursor.getLong(3);
        historyText.append('"').append(massageHistoryField(format.format(timestamp))).append("\",");

        // Above we're preserving the old ordering of columns which had formatted data in position 5

        historyText.append('"').append(massageHistoryField(cursor.getString(4))).append("\"\r\n");
      }
      return historyText;
    }
  }
  
  void clearHistory() {
    SQLiteOpenHelper helper = new DBHelper(activity);
    try (SQLiteDatabase db = helper.getWritableDatabase()) {
      db.delete(DBHelper.TABLE_NAME, null, null);
    } catch (SQLException sqle) {
      Log.w(TAG, sqle);
    }
  }

  static Uri saveHistory(String history) {
    File bsRoot = new File(Environment.getExternalStorageDirectory(), "BarcodeScanner");
    File historyRoot = new File(bsRoot, "History");
    if (!historyRoot.exists() && !historyRoot.mkdirs()) {
      Log.w(TAG, "Couldn't make dir " + historyRoot);
      return null;
    }
    File historyFile = new File(historyRoot, "history-" + System.currentTimeMillis() + ".csv");
    try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(historyFile), StandardCharsets.UTF_8)) {
      out.write(history);
      return Uri.parse("file://" + historyFile.getAbsolutePath());
    } catch (IOException ioe) {
      Log.w(TAG, "Couldn't access file " + historyFile + " due to " + ioe);
      return null;
    }
  }

  private static String massageHistoryField(String value) {
    return value == null ? "" : DOUBLE_QUOTE.matcher(value).replaceAll("\"\"");
  }

}
