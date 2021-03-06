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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.datastore.ReadPolicy.Consistency;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.gson.*;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comment data. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final DatastoreServiceConfig DEFAULT_DATASTORE_CONFIG =
      DatastoreServiceConfig.Builder.withReadPolicy(new ReadPolicy(Consistency.STRONG)).deadline(5.0);
  private static final int MIN_COMMENT_LIMIT = 5;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(Comment.ENTITY_NAME_PARAM).addSort(Comment.TIME_PARAM, SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(DEFAULT_DATASTORE_CONFIG);
    PreparedQuery results = datastore.prepare(query);

    List<Comment> comments = new ArrayList<>();

    int commentLimit = getCommentLimit(request);
    String languageCode = getLanguageCode(request);

    results.asList(FetchOptions.Builder.withLimit(commentLimit)).forEach(entity -> {
      Comment comment = commentFromEntity(entity, languageCode);
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

  private Comment commentFromEntity(Entity entity, String languageCode) {
    String text = (String) entity.getProperty(Comment.TEXT_PARAM);
    long timestampMillis = (long) entity.getProperty(Comment.TIME_PARAM);

    // Do the translation.
    Translate translate = TranslateOptions.getDefaultInstance().getService();
    Translation translation =
        translate.translate(text, Translate.TranslateOption.targetLanguage(languageCode));
    String translatedText = translation.getTranslatedText();

    Comment comment = new Comment(translatedText, timestampMillis);
    return comment;
  }

  private int getCommentLimit(HttpServletRequest request) {
    String commentLimitString = request.getParameter("limit");

    return Math.max(Integer.parseInt(commentLimitString), MIN_COMMENT_LIMIT);
  }

  private String getLanguageCode(HttpServletRequest request) {
    String languageCode = request.getParameter("lang");
    if (languageCode == null) {
      return "en";
    } else {
      return languageCode;
    }
  }
}
