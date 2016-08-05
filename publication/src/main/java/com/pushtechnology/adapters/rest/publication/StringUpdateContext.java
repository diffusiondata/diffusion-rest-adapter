/*******************************************************************************
 * Copyright (C) 2016 Push Technology Ltd.
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

import static com.pushtechnology.adapters.rest.publication.UpdateTopicCallback.INSTANCE;

import java.nio.charset.Charset;

import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.ValueUpdater;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.datatype.binary.BinaryDataType;

/**
 * Implementation of {@link UpdateContext} for {@link String} values.
 * @author Push Technology Limited
 */
/*package*/ final class StringUpdateContext extends AbstractUpdateContext<String> {
    private static final BinaryDataType DATA_TYPE = Diffusion.dataTypes().binary();
    private final ValueUpdater<Binary> updater;
    private final String topicPath;

    /*package*/ StringUpdateContext(Session session, ValueUpdater<Binary> updater, String topicPath) {
        super(session);
        this.updater = updater;
        this.topicPath = topicPath;
    }

    @Override
    protected void publishValue(String value) {
        updater.update(topicPath, DATA_TYPE.readValue(value.getBytes(Charset.forName("UTF-8"))), topicPath, INSTANCE);
    }
}
