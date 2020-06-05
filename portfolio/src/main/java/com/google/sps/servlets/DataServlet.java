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
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.*;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final int MIN_COMMENT_LIMIT = 5;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(Comment.ENTITY_NAME_PARAM).addSort(Comment.TIME_PARAM, SortDirection.DESCENDING);
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List comments = new ArrayList<>();

    int commentLimit = getCommentLimit(request);

    results.asList(FetchOptions.Builder.withLimit(commentLimit)).forEach(entity -> {
      Comment comment = commentFromEntity(entity);
      comments.add(comment);
    });

    Gson gson = new Gson();
    String json = gson.toJson(comments);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = request.getParameter(Comment.ENTITY_NAME_PARAM);
    if (comment != null) {
      long timestampMillis = System.currentTimeMillis();

      Entity commentEntity = new Entity(Comment.ENTITY_NAME_PARAM);
      commentEntity.setProperty(Comment.TEXT_PARAM, comment);
      commentEntity.setProperty(Comment.TIME_PARAM, timestampMillis);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(commentEntity);
    }
    response.sendRedirect("/index.html#comment-container");
  }

  public Comment commentFromEntity(Entity entity) {
    String text = (String) entity.getProperty(Comment.TEXT_PARAM);
    long timestampMillis = (long) entity.getProperty(Comment.TIME_PARAM);

    Comment comment = new Comment(text, timestampMillis);
    return comment;
  }

  public int getCommentLimit(HttpServletRequest request) {
    String commentLimitString = request.getParameter("limit");
    if (commentLimitString == null) return MIN_COMMENT_LIMIT;

    int commentLimit = Integer.parseInt(commentLimitString);
    if (commentLimit < MIN_COMMENT_LIMIT) commentLimit = MIN_COMMENT_LIMIT;

    return commentLimit;
  }
}
