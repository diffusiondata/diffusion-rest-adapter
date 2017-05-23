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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metrics.PublicationListener.PublicationCompletionListener;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater.UpdateContextCallback;

import net.jcip.annotations.Immutable;

/**
 * Callback for topic updates.
 *
 * @author Push Technology Limited
 */
@Immutable
public class UpdateTopicCallback implements UpdateContextCallback<String> {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateTopicCallback.class);
    private final PublicationCompletionListener completionListener;

    /**
     * Constructor.
     */
    /*package*/ UpdateTopicCallback(PublicationCompletionListener completionListener) {
        this.completionListener = completionListener;
    }

    @Override
    public void onSuccess(String topicPath) {
        LOG.trace("Updated topic {}", topicPath);
        completionListener.onPublication();
    }

    @Override
    public void onError(String topicPath, ErrorReason errorReason) {
        LOG.warn("Failed to update topic {} {}", topicPath, errorReason);
        completionListener.onPublicationFailed(errorReason);
    }
}
