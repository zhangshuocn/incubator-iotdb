/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.cluster.query.coordinatornode.manager;

import com.alipay.sofa.jraft.entity.PeerId;
import org.apache.iotdb.db.qp.physical.PhysicalPlan;

/**
 * Manage a single query.
 */
public interface IClusterSingleQueryManager {

  /**
   * Divide physical plan into several sub physical plans according to timeseries full path.
   */
  void dividePhysicalPlan();

  /**
   * Get physical plan of select path
   *
   * @param fullPath Timeseries full path in select paths
   */
  PhysicalPlan getSelectPathPhysicalPlan(String fullPath);

  /**
   * Get physical plan of filter path
   *
   * @param fullPath Timeseries full path in filter
   */
  PhysicalPlan getFilterPathPhysicalPlan(String fullPath);

  /**
   * Set reader node of a data group
   *
   * @param groupId data group id
   * @param readerNode peer id
   */
  void setDataGroupReaderNode(String groupId, PeerId readerNode);

  /**
   * Get reader node of a data group by group id
   * @param groupId data group id
   * @return peer id of reader node
   */
  PeerId getDataGroupReaderNode(String groupId);

  /**
   * Release query resource
   */
  void releaseQueryResource();
}