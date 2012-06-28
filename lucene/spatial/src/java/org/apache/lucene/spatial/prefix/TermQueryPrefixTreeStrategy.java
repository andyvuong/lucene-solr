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

package org.apache.lucene.spatial.prefix;

import com.spatial4j.core.exception.UnsupportedSpatialOperation;
import com.spatial4j.core.query.SpatialArgs;
import com.spatial4j.core.query.SpatialOperation;
import com.spatial4j.core.shape.Shape;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.spatial.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.prefix.tree.Node;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;

import java.util.List;

/**
 * A basic implementation using a large {@link TermsFilter} of all the nodes from
 * {@link SpatialPrefixTree#getNodes(com.spatial4j.core.shape.Shape, int, boolean)}.
 */
public class TermQueryPrefixTreeStrategy extends PrefixTreeStrategy {

  public TermQueryPrefixTreeStrategy(SpatialPrefixTree grid) {
    super(grid);
  }

  @Override
  public Filter makeFilter(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    final SpatialOperation op = args.getOperation();
    if (! SpatialOperation.is(op, SpatialOperation.IsWithin, SpatialOperation.Intersects, SpatialOperation.BBoxWithin, SpatialOperation.BBoxIntersects))
      throw new UnsupportedSpatialOperation(op);

    Shape shape = args.getShape();
    int detailLevel = grid.getMaxLevelForPrecision(shape, args.getDistPrecision());
    List<Node> cells = grid.getNodes(shape, detailLevel, false);
    TermsFilter filter = new TermsFilter();
    for (Node cell : cells) {
      filter.addTerm(new Term(fieldInfo.getFieldName(), cell.getTokenString()));
    }
    return filter;
  }

}
