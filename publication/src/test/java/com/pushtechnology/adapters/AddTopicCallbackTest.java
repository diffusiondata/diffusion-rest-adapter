package com.pushtechnology.adapters;

import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.EXISTS;
import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.EXISTS_MISMATCH;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.publication.AddTopicCallback;
import com.pushtechnology.adapters.rest.publication.PublishingClient;

/**
 * Unit tests for {@link AddTopicCallback}.
 *
 * @author Matt Champion on 13/07/2016
 */
public final class AddTopicCallbackTest {
    @Mock
    private PublishingClient.InitialiseCallback callback;

    private AddTopicCallback addTopicCallback;

    @Before
    public void setUp() {
        initMocks(this);

        addTopicCallback = new AddTopicCallback("topic", callback);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void onTopicAdded() {
        addTopicCallback.onTopicAdded("topic");

        verify(callback).onTopicAdded("topic");
    }

    @Test
    public void onTopicAddFailedExists() {
        addTopicCallback.onTopicAddFailed("topic", EXISTS);

        verify(callback).onTopicAdded("topic");
    }

    @Test
    public void onTopicAddFailed() {
        addTopicCallback.onTopicAddFailed("topic", EXISTS_MISMATCH);
    }

    @Test
    public void onDiscard() {
        addTopicCallback.onDiscard();
    }
}
