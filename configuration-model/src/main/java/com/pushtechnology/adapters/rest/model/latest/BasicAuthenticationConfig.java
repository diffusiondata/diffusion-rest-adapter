/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.model.latest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

/**
 * Basic authentication configuration. Versions 13, 14, 15 and 16.
 * <p>
 * Description of the basic authentication parameters.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
@ToString(of = {"userid"})
public class BasicAuthenticationConfig {
    String userid;
    String password;
}
