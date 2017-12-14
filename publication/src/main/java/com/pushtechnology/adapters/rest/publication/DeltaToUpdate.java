/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.pushtechnology.adapters.rest.publication;

import java.util.function.Function;

import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.content.ContentFactory;
import com.pushtechnology.diffusion.client.content.update.ContentUpdateFactory;
import com.pushtechnology.diffusion.client.content.update.Update;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.BinaryDelta;
import com.pushtechnology.diffusion.datatype.DeltaType;

/**
 * Convert a {@link BinaryDelta} to an applicative update.
 *
 * @author Push Technology Limited
 */
@SuppressWarnings("deprecation")
public final class DeltaToUpdate implements Function<BinaryDelta, Update> {
    private static final ContentFactory CONTENT_FACTORY = Diffusion.content();
    private final ContentUpdateFactory updateFactory;
    private final DeltaType<?, BinaryDelta> deltaType;

    /**
     * Constructor.
     */
    /*package*/ DeltaToUpdate(Session session, DeltaType<?, BinaryDelta> deltaType) {
        updateFactory = session
            .feature(TopicUpdateControl.class)
            .updateFactory(ContentUpdateFactory.class);
        this.deltaType = deltaType;
    }

    @Override
    public Update apply(BinaryDelta delta) {
        return updateFactory.apply(CONTENT_FACTORY.newContent(deltaType.toBytes(delta).toByteArray()));
    }
}