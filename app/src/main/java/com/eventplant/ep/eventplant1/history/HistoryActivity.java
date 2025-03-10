/*
 * Copyright 2012 ZXing authors
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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.eventplant.ep.eventplant1.R;

/**
 * The activity for interacting with the scan history.
 */
public final class HistoryActivity extends ListActivity {

  private static final String TAG = HistoryActivity.class.getSimpleName();

  private HistoryManager historyManager;
  private ArrayAdapter<HistoryItem> adapter;
  private CharSequence originalTitle;

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    this.historyManager = new HistoryManager(this);
    adapter = new HistoryItemAdapter(this);
    setListAdapter(adapter);
    View listview = getListView();
    registerForContextMenu(listview);
    originalTitle = getTitle();
  }

  @Override
  protected void onResume() {
    super.onResume();
    reloadHistoryItems();
  }

  private void reloadHistoryItems() {
    Iterable<HistoryItem> items = historyManager.buildHistoryItems();
    adapter.clear();
    for (HistoryItem item : items) {
      adapter.add(item);
    }
    setTitle("스캔리스트" + " (" + adapter.getCount() + ')');
    if (adapter.isEmpty()) {
      adapter.add(new HistoryItem(null, null, 0));
    }
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
//    if (adapter.getItem(position).getResult() != null) {
//      Intent intent = new Intent(this, CaptureActivity.class);
//      intent.putExtra(Intents.History.ITEM_NUMBER, position);
//      setResult(Activity.RESULT_OK, intent);
//      finish();
//    }
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu,
                                  View v,
                                  ContextMenu.ContextMenuInfo menuInfo) {
    int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
    if (position >= adapter.getCount() || adapter.getItem(position).getResult() != null) {
      menu.add(Menu.NONE, position, position, R.string.history_clear_one_history_text);
    } // else it's just that dummy "Empty" message
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    int position = item.getItemId();
    historyManager.deleteHistoryItem(position);
    reloadHistoryItems();
    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (historyManager.hasHistoryItems()) {
      MenuInflater menuInflater = getMenuInflater();
      menuInflater.inflate(R.menu.history, menu);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case R.id.menu_history_clear_text:
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.msg_sure);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int i2) {
            historyManager.clearHistory();
            dialog.dismiss();
            finish();
          }
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.show();
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

}
