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

package org.apache.iotdb.db.query.aggregation.impl;

import java.io.IOException;
import java.util.List;
import org.apache.iotdb.db.exception.ProcessorException;
import org.apache.iotdb.db.query.aggregation.AggreResultData;
import org.apache.iotdb.db.query.aggregation.AggregateFunction;
import org.apache.iotdb.db.query.aggregation.AggregationConstant;
import org.apache.iotdb.db.query.reader.IPointReader;
import org.apache.iotdb.db.query.reader.merge.EngineReaderByTimeStamp;
import org.apache.iotdb.db.utils.TsPrimitiveType;
import org.apache.iotdb.tsfile.file.header.PageHeader;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.tsfile.read.common.BatchData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountAggrFunc extends AggregateFunction {

  private static final Logger LOGGER = LoggerFactory.getLogger(CountAggrFunc.class);

  public CountAggrFunc() {
    super(AggregationConstant.COUNT, TSDataType.INT64);
  }

  @Override
  public void init() {
    resultData.reSet();
    resultData.setTimestamp(0);
    resultData.setLongRet(0);
  }

  @Override
  public AggreResultData getResult() {
    return resultData;
  }

  @Override
  public void calculateValueFromPageHeader(PageHeader pageHeader) {
    LOGGER.debug("PageHeader>>>>>>>>>>>>num of rows:{}, minTimeStamp:{}, maxTimeStamp{}",
        pageHeader.getNumOfValues(), pageHeader.getMinTimestamp(), pageHeader.getMaxTimestamp());
    long preValue = resultData.getLongRet();
    preValue += pageHeader.getNumOfValues();
    resultData.setLongRet(preValue);

  }

  @Override
  public void calculateValueFromPageData(BatchData dataInThisPage, IPointReader unsequenceReader)
      throws IOException, ProcessorException {
    while (dataInThisPage.hasNext() && unsequenceReader.hasNext()) {
      if (dataInThisPage.currentTime() == unsequenceReader.current().getTimestamp()) {
        dataInThisPage.next();
        unsequenceReader.next();
      } else if (dataInThisPage.currentTime() < unsequenceReader.current().getTimestamp()) {
        dataInThisPage.next();
      } else {
        unsequenceReader.next();
      }
      long preValue = resultData.getLongRet();
      preValue += 1;
      resultData.setLongRet(preValue);
    }

    if (dataInThisPage.hasNext()) {
      long preValue = resultData.getLongRet();
      preValue += (dataInThisPage.length() - dataInThisPage.getCurIdx());
      resultData.setLongRet(preValue);
    }
  }

  @Override
  public void calculateValueFromPageData(BatchData dataInThisPage, IPointReader unsequenceReader,
      long bound) throws IOException {
    int cnt = 0;
    while (dataInThisPage.hasNext() && unsequenceReader.hasNext()) {
      if (dataInThisPage.currentTime() == unsequenceReader.current().getTimestamp()) {
        if (dataInThisPage.currentTime() >= bound) {
          break;
        }
        dataInThisPage.next();
        unsequenceReader.next();
      } else if (dataInThisPage.currentTime() < unsequenceReader.current().getTimestamp()) {
        if (dataInThisPage.currentTime() >= bound) {
          break;
        }
        dataInThisPage.next();
      } else {
        if (unsequenceReader.current().getTimestamp() >= bound) {
          break;
        }
        unsequenceReader.next();
      }
      cnt++;
    }

    while (dataInThisPage.hasNext() && dataInThisPage.currentTime() < bound) {
      dataInThisPage.next();
      cnt++;
    }
    long preValue = resultData.getLongRet();
    preValue += cnt;
    resultData.setLongRet(preValue);
  }

  @Override
  public void calculateValueFromUnsequenceReader(IPointReader unsequenceReader)
      throws IOException {
    int cnt = 0;
    while (unsequenceReader.hasNext()) {
      unsequenceReader.next();
      cnt++;
    }
    long preValue = resultData.getLongRet();
    preValue += cnt;
    resultData.setLongRet(preValue);
  }

  @Override
  public void calculateValueFromUnsequenceReader(IPointReader unsequenceReader, long bound)
      throws IOException {
    int cnt = 0;
    while (unsequenceReader.hasNext() && unsequenceReader.current().getTimestamp() < bound) {
      unsequenceReader.next();
      cnt++;
    }
    long preValue = resultData.getLongRet();
    preValue += cnt;
    resultData.setLongRet(preValue);
  }

  @Override
  public void calcAggregationUsingTimestamps(List<Long> timestamps,
      EngineReaderByTimeStamp dataReader) throws IOException {
    int cnt = 0;
    for (long time : timestamps) {
      TsPrimitiveType value = dataReader.getValueInTimestamp(time);
      if (value != null) {
        cnt++;
      }
    }

    long preValue = resultData.getLongRet();
    preValue += cnt;
    resultData.setLongRet(preValue);
  }

  @Override
  public boolean isCalculatedAggregationResult() {
    return false;
  }
}
