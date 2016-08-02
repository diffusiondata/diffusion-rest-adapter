package com.pushtechnology.adapters.rest.topic.management;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;

/**
 * Unit tests for {@link TopicSetupCallback}.
 *
 * @author Matt Champion on 02/08/2016
 */
public final class TopicSetupCallbackTest {
    @Mock
    private TopicControl.AddCallback delegate;

    private TopicControl.AddCallback callback;

    @Before
    public void setUp() {
        initMocks(this);

        callback = new TopicSetupCallback(delegate);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void onTopicAdded() {
        callback.onTopicAdded("a/topic");

        verify(delegate).onTopicAdded("a/topic");
    }

    @Test
    public void onTopicAddFailed() {
        callback.onTopicAddFailed("a/topic", TopicAddFailReason.TOPIC_NOT_FOUND);

        verify(delegate).onTopicAddFailed("a/topic", TopicAddFailReason.TOPIC_NOT_FOUND);
    }

    @Test
    public void onDiscard() {
        callback.onDiscard();

        verify(delegate).onDiscard();
    }

    @Test
    public void topicAlreadyPresent() {
        callback.onTopicAddFailed("a/topic", TopicAddFailReason.EXISTS);

        verify(delegate).onTopicAdded("a/topic");
    }
}
