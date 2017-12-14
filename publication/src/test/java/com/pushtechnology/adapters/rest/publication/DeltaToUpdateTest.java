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

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.diffusion.client.content.update.ContentUpdateFactory;
import com.pushtechnology.diffusion.client.content.update.Update;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.BinaryDelta;
import com.pushtechnology.diffusion.datatype.Bytes;
import com.pushtechnology.diffusion.datatype.DeltaType;
import com.pushtechnology.diffusion.datatype.binary.Binary;

/**
 * Unit tests for {@link DeltaToUpdate}.
 *
 * @author Matt Champion 14/12/2017
 */
@SuppressWarnings("deprecation")
public final class DeltaToUpdateTest {
    @Mock
    private Session session;
    @Mock
    private Binary binary;
    @Mock
    private DeltaType<Binary, BinaryDelta> deltaType;
    @Mock
    private BinaryDelta delta;
    @Mock
    private Bytes bytes;
    @Mock
    private Update update;
    @Mock
    private TopicUpdateControl updateControl;
    @Mock
    private ContentUpdateFactory updateFactory;

    @Before
    public void setUp() {
        initMocks(this);

        when(deltaType.diff(binary, binary)).thenReturn(delta);
        when(deltaType.toBytes(delta)).thenReturn(bytes);
        when(bytes.toByteArray()).thenReturn(new byte[0]);
        when(session.feature(TopicUpdateControl.class)).thenReturn(updateControl);
        when(updateControl.updateFactory(ContentUpdateFactory.class)).thenReturn(updateFactory);
        when(updateFactory.apply(isNotNull())).thenReturn(update);
    }

    @Test
    public void apply() throws Exception {
        final DeltaToUpdate deltaToUpdate = new DeltaToUpdate(session, deltaType);

        verify(session).feature(TopicUpdateControl.class);

        final Update update = deltaToUpdate.apply(delta);

        assertNotNull(update);

        verify(session).feature(TopicUpdateControl.class);
        verify(updateControl).updateFactory(ContentUpdateFactory.class);
    }

}
