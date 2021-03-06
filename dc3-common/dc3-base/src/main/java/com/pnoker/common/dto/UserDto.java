/*
 * Copyright 2019 Pnoker. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pnoker.common.dto;

import com.pnoker.common.base.Converter;
import com.pnoker.common.bean.Pages;
import com.pnoker.common.model.User;
import lombok.*;
import org.springframework.beans.BeanUtils;

/**
 * User DTO
 *
 * @author pnoker
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UserDto extends User implements Converter<User, UserDto> {

    private Pages page;

    @Override
    public void convertToDo(User user) {
        BeanUtils.copyProperties(this, user);
    }

    @Override
    public UserDto convert(User user) {
        BeanUtils.copyProperties(user, this);
        return this;
    }
}