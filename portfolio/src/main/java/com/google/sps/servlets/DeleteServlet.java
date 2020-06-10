// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceConfig.Builder;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.datastore.ReadPolicy.Consistency;
import com.google.gson.*;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that deletes datastore data. */
@WebServlet("/delete-data")
public class DeleteServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(Comment.ENTITY_NAME_PARAM);
    
    DatastoreServiceConfig datastoreConfig = DatastoreServiceConfig.Builder.withReadPolicy(
      new ReadPolicy(Consistency.STRONG)).deadline(5.0);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(datastoreConfig);
    PreparedQuery results = datastore.prepare(query);

    List<Key> keyList = keyListBuilder(results);
    datastore.delete(keyList);
  }

  private List keyListBuilder(PreparedQuery results) {
      List<Key> keyList = new ArrayList<>();
      for (Entity entity : results.asIterable()) {
        keyList.add(entity.getKey());
      }
      return keyList;
  }
}
