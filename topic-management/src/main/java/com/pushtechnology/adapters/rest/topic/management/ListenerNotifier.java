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

package com.pushtechnology.adapters.rest.topic.management;

import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;

/**
 * Notifier for {@link com.pushtechnology.adapters.rest.metrics.TopicCreationListener}.
 * @author Matt Champion 13/05/2017
 */
/*package*/ interface ListenerNotifier {

    /**
     * Notify {@link com.pushtechnology.adapters.rest.metrics.TopicCreationListener} of a topic creation.
     */
    void notifyTopicCreated();

    /**
     * Notify {@link com.pushtechnology.adapters.rest.metrics.TopicCreationListener} of a topic creation failure.
     */
    void notifyTopicCreationFailed(TopicAddFailReason reason);
}
