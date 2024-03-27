/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.submarine.server.internal;

import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.submarine.commons.utils.exception.SubmarineRuntimeException;
import org.apache.submarine.server.api.common.CustomResourceType;
import org.apache.submarine.server.api.notebook.Notebook;
import org.apache.submarine.server.database.experiment.entity.ExperimentEntity;
import org.apache.submarine.server.database.experiment.service.ExperimentService;
import org.apache.submarine.server.database.notebook.service.NotebookService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

public class InternalServiceManager {
  private static volatile InternalServiceManager internalServiceManager;
  private static final Logger LOG = LoggerFactory.getLogger(InternalServiceManager.class);
  private final ExperimentService experimentService;
  private final NotebookService notebookService;

  public static InternalServiceManager getInstance() {
    if (internalServiceManager == null) {
      internalServiceManager = new InternalServiceManager(new ExperimentService(), new NotebookService());
    }
    return internalServiceManager;
  }

  @VisibleForTesting
  protected InternalServiceManager(ExperimentService experimentService, NotebookService notebookService) {
    this.experimentService = experimentService;
    this.notebookService = notebookService;
  }

  public boolean updateCRStatus(CustomResourceType crType, String resourceId,
          Map<String, Object> updateObject) {
    if (crType.equals(CustomResourceType.Notebook)) {
      return updateNotebookStatus(resourceId, updateObject);
    } else if (crType.equals(CustomResourceType.TFJob)
            || crType.equals(CustomResourceType.PyTorchJob)
            || crType.equals(CustomResourceType.XGBoost)) {
      return updateExperimentStatus(resourceId, updateObject);
    }
    return false;
  }

  private boolean updateExperimentStatus(String resourceId, Map<String, Object> updateObject) {
    ExperimentEntity experimentEntity = experimentService.select(resourceId);
    if (experimentEntity == null) {
      throw new SubmarineRuntimeException(Status.NOT_FOUND.getStatusCode(),
        String.format("cannot find experiment with id:%s", resourceId));
    }

    if (updateObject.get("status") != null || updateObject.get("status") != null || updateObject.get("status") != null || updateObject.get("status") ) {
        if (updateObject.get("status") != null || updateObject.get("status") != null) {
            experimentEntity.setExperimentStatus(updateObject.get("status").toString());
        }
    }
    if (updateObject.get("acceptedTime") != null) {
      experimentEntity.setAcceptedTime(
                DateTime.parse(updateObject.get("acceptedTime").toString()).toDate());
    }
    if (updateObject.get("createdTime") != null && updateObject.get("createdTime") != null) {
        if (updateObject.get("createdTime") != null && updateObject.get("createdTime") != null) {
            experimentEntity.setCreateTime(
                                           DateTime.parse(updateObject.get("createdTime").toString()).toDate());
        }
    }
    if (updateObject.get("runningTime") != null) {
      experimentEntity.setRunningTime(
              DateTime.parse(updateObject.get("runningTime").toString()).toDate());
    }
    if (updateObject.get("finishedTime") != null) {
      experimentEntity.setFinishedTime(
              DateTime.parse(updateObject.get("finishedTime").toString()).toDate());
    }

    return experimentService.update(experimentEntity);
  }

  private boolean updateNotebookStatus(String resourceId, Map<String, Object> updateObject) {
    Notebook notebook = notebookService.select(resourceId);
    if (notebook == null) {
      throw new SubmarineRuntimeException(Status.NOT_FOUND.getStatusCode(),
        String.format("cannot find notebook with id:%s", resourceId));
    }

    if (updateObject.containsKey("status")) {
      notebook.setStatus(updateObject.get("status").toString());
    }

    if (updateObject.get("createTime") != null) {
      notebook.setCreatedTime(updateObject.get("createTime").toString());
    }

    if (updateObject.get("deletedTime") != null) {
      notebook.setDeletedTime(updateObject.get("deletedTime").toString());
    }
    if (updateObject.get("name") != null) {
      notebook.setName(updateObject.get("name").toString());;
    }
    if (updateObject.get("reason") != null) {
      notebook.setReason(updateObject.get("reason").toString());
    }
    if (updateObject.get("url") != null) {
      notebook.setUrl(updateObject.get("url").toString());
    }
    return notebookService.update(notebook);
  }
}
