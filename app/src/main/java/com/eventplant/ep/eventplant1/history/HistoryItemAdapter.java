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

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eventplant.ep.eventplant1.R;
import com.google.zxing.Result;

import java.util.ArrayList;

final class HistoryItemAdapter extends ArrayAdapter<HistoryItem> {

  private final Context activity;

  HistoryItemAdapter(Context activity) {
    super(activity, R.layout.history_list_item, new ArrayList<HistoryItem>());
    this.activity = activity;
  }

  @Override
  public View getView(int position, View view, ViewGroup viewGroup) {
    View layout;
    if (view instanceof LinearLayout) {
      layout = view;
    } else {
      LayoutInflater factory = LayoutInflater.from(activity);
      layout = factory.inflate(R.layout.history_list_item, viewGroup, false);
    }

    HistoryItem item = getItem(position);
    Result result = item.getResult();

    CharSequence title;
    CharSequence detail;
    long detail1;

    if (result != null) {
      title = result.getText();
      String str = result.getTimestamp()+"";

      detail = "("+str.substring(0,4)+"/"+str.substring(4,6)+"/"+str.substring(6,8)+" "+str.substring(8,10)+":"+str.substring(10,12)+":"+str.substring(12)+")";
      //detail = item.getDisplayAndDetails();
    } else {
      Resources resources = getContext().getResources();
      title = resources.getString(R.string.history_empty);
      detail = resources.getString(R.string.history_empty_detail);
    }

    ((TextView) layout.findViewById(R.id.history_title)).setText(title);
    ((TextView) layout.findViewById(R.id.history_detail)).setText(detail);

    return layout;
  }

}
