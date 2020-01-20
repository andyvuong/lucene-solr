/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.store.shared;

import com.google.common.annotations.VisibleForTesting;
import org.apache.solr.cloud.ZkController;
import org.apache.solr.store.blob.metadata.BlobCoreSyncer;
import org.apache.solr.store.blob.process.BlobDeleteManager;
import org.apache.solr.store.blob.process.BlobProcessUtil;
import org.apache.solr.store.blob.process.CorePullTracker;
import org.apache.solr.store.blob.provider.BlobStorageProvider;
import org.apache.solr.store.shared.metadata.SharedShardMetadataController;

/**
 * Provides access to Shared Store processes. Note that this class is meant to be 
 * more generic in the future and provide a cleaner API but for now we'll expose
 * the underlying implementations
 */
public class SharedStoreManager {
  
  private ZkController zkController;
  private SharedShardMetadataController sharedShardMetadataController;
  private BlobStorageProvider blobStorageProvider;
  private BlobDeleteManager blobDeleteManager;
  private BlobProcessUtil blobProcessUtil;
  private CorePullTracker corePullTracker;
  private BlobCoreSyncer blobCoreSyncer;
  private SharedCoreConcurrencyController sharedCoreConcurrencyController;

  public SharedStoreManager(ZkController controller) {
    zkController = controller;
    // initialize BlobProcessUtil with the SharedStoreManager for background processes to be ready
    blobProcessUtil = new BlobProcessUtil(zkController.getCoreContainer());
    blobCoreSyncer = new BlobCoreSyncer();
    sharedCoreConcurrencyController = new SharedCoreConcurrencyController(zkController.getCoreContainer());
  }
  
  @VisibleForTesting
  public void initBlobStorageProvider(BlobStorageProvider blobStorageProvider) {
    this.blobStorageProvider = blobStorageProvider;
  }
  
  @VisibleForTesting
  public void initBlobProcessUtil(BlobProcessUtil processUtil) {
    if (blobProcessUtil != null) {
      blobProcessUtil.shutdown();
    }
    blobProcessUtil = processUtil;
  }
  
  /*
   * Initiates a SharedShardMetadataController if it doesn't exist and returns one 
   */
  public SharedShardMetadataController getSharedShardMetadataController() {
    if (sharedShardMetadataController != null) {
      return sharedShardMetadataController;
    }
    sharedShardMetadataController = new SharedShardMetadataController(zkController.getSolrCloudManager());
    return sharedShardMetadataController;
  }
  
  /*
   * Initiates a BlobStorageProvider if it doesn't exist and returns one 
   */
  public BlobStorageProvider getBlobStorageProvider() {
    if (blobStorageProvider != null) {
      return blobStorageProvider;
    }
    blobStorageProvider = new BlobStorageProvider();
    return blobStorageProvider;
  }
  
  public BlobDeleteManager getBlobDeleteManager() {
    if (blobDeleteManager != null) {
      return blobDeleteManager;
    }
    blobDeleteManager = new BlobDeleteManager(getBlobStorageProvider().getClient());
    return blobDeleteManager;
  }
  
  public BlobProcessUtil getBlobProcessManager() {
    if (blobProcessUtil != null) {
      return blobProcessUtil;
    }
    blobProcessUtil = new BlobProcessUtil(zkController.getCoreContainer());
    return blobProcessUtil;
  }
  
  public CorePullTracker getCorePullTracker() {
    if (corePullTracker != null) {
      return corePullTracker ;
    }
    corePullTracker = new CorePullTracker();
    return corePullTracker ;
  }
  
  public BlobCoreSyncer getBlobCoreSyncer() {
    return blobCoreSyncer;
  }

  public SharedCoreConcurrencyController getSharedCoreConcurrencyController() {
    return sharedCoreConcurrencyController;
  }

  @VisibleForTesting
  public void initConcurrencyController(SharedCoreConcurrencyController concurrencyController) {
    this.sharedCoreConcurrencyController = concurrencyController;
  }
  
  @VisibleForTesting
  public void initBlobDeleteManager(BlobDeleteManager blobDeleteManager) {
    if (this.blobDeleteManager != null) {
      blobDeleteManager.shutdown();
    }
    this.blobDeleteManager = blobDeleteManager;
  }

}