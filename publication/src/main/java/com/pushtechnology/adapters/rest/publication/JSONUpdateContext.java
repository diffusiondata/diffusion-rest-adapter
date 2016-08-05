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

import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.ValueUpdater;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Implementation of {@link UpdateContext} for {@link JSON} values.
 * @author Push Technology Limited
 */
/*package*/ final class JSONUpdateContext extends AbstractUpdateContext<JSON> {
    private final ValueUpdater<JSON> updater;
    private final String topicPath;

    /*package*/ JSONUpdateContext(Session session, ValueUpdater<JSON> updater, String topicPath) {
        super(session);
        this.updater = updater;
        this.topicPath = topicPath;
    }

    @Override
    protected void publishValue(JSON value) {
        updater.update(topicPath, value, topicPath, INSTANCE);
    }
}
