/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sps.data;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;

/**
 * Cloud function that runs when triggered by a PubSubMessa e. Deletes all posts that are older th
 * 24 hours. Note: This uses google clou datastore since a cloud function does not have access t app
 * engine datstor properties. Cloud datastore uses the same database as app engine. The only
 * differences are in API usages and methods.
 */
public class DeletePosts implements BackgroundFunction<PubSubMessage> {
  private Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

  @Override
  public void accept(PubSubMessage message, Context context) {
    DeletePostService deleteService = new DeletePostService(datastore);
    deleteService.deleteOldPosts();
  }
}
