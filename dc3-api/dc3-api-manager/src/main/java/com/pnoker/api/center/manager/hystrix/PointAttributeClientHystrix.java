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

package com.pnoker.api.center.manager.hystrix;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pnoker.api.center.manager.feign.PointAttributeClient;
import com.pnoker.common.bean.R;
import com.pnoker.common.dto.PointAttributeDto;
import com.pnoker.common.model.Dictionary;
import com.pnoker.common.model.PointAttribute;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>PointAttributeClientHystrix
 *
 * @author pnoker
 */
@Slf4j
@Component
public class PointAttributeClientHystrix implements FallbackFactory<PointAttributeClient> {

    @Override
    public PointAttributeClient create(Throwable throwable) {
        String message = throwable.getMessage() == null ? "No available server for client: DC3-MANAGER" : throwable.getMessage();
        log.error("Hystrix:{}", message, throwable);

        return new PointAttributeClient() {

            @Override
            public R<PointAttribute> add(PointAttribute pointAttribute) {
                return R.fail(message);
            }

            @Override
            public R<Boolean> delete(Long id) {
                return R.fail(message);
            }

            @Override
            public R<PointAttribute> update(PointAttribute pointAttribute) {
                return R.fail(message);
            }

            @Override
            public R<PointAttribute> selectById(Long id) {
                return R.fail(message);
            }

            @Override
            public R<Page<PointAttribute>> list(PointAttributeDto profileInfoDto) {
                return R.fail(message);
            }

        };
    }
}