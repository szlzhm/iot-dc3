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

package com.pnoker.common.sdk.quartz.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pnoker.api.center.manager.feign.ScheduleClient;
import com.pnoker.common.bean.Pages;
import com.pnoker.common.bean.R;
import com.pnoker.common.constant.Common;
import com.pnoker.common.dto.ScheduleDto;
import com.pnoker.common.model.Device;
import com.pnoker.common.model.Schedule;
import com.pnoker.common.sdk.init.DeviceDriver;
import com.pnoker.common.sdk.quartz.service.QuartzService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author pnoker
 */
@Slf4j
@Service
public class QuartzServiceImpl implements QuartzService {

    @Resource
    private Scheduler scheduler;
    @Resource
    private DeviceDriver deviceDriver;
    @Resource
    private ScheduleClient scheduleClient;

    @Override
    @SneakyThrows
    public void initial() {
        Map<Long, Device> deviceMap = deviceDriver.getDeviceMap();
        for (Device device : deviceMap.values()) {
            List<Schedule> schedules = getScheduleList(device.getId());
            for (Schedule schedule : schedules) {
                runJob(device.getName(), schedule);
            }
        }
    }

    @Override
    @SneakyThrows
    public void startJob(String group, Schedule schedule) {
        JobKey jobKey = JobKey.jobKey(schedule.getName(), group);
        scheduler.triggerJob(jobKey);
        R<Schedule> r = scheduleClient.update(schedule.setStatus((short) 1));
        if (!r.isOk()) {
            log.error("start device({}) job({}) failed ({})", group, schedule.getName(), r.getMessage());
        }
    }

    @Override
    @SneakyThrows
    public void deleteJob(String group, Schedule schedule) {
        JobKey jobKey = JobKey.jobKey(schedule.getName(), group);
        scheduler.deleteJob(jobKey);
    }

    @Override
    @SneakyThrows
    public void update(String group, Schedule schedule) {
        TriggerKey triggerKey = TriggerKey.triggerKey(schedule.getName(), group);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(schedule.getCornExpression());
        trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
        scheduler.rescheduleJob(triggerKey, trigger);
        R<Schedule> r = scheduleClient.update(schedule.setStatus((short) 1));
        if (!r.isOk()) {
            log.error("update device({}) job({}) failed ({})", group, schedule.getName(), r.getMessage());
        }
    }

    @Override
    @SneakyThrows
    public void pauseJob(String group, Schedule schedule) {
        JobKey jobKey = JobKey.jobKey(schedule.getName(), group);
        scheduler.pauseJob(jobKey);
        R<Schedule> r = scheduleClient.update(schedule.setStatus((short) 2));
        if (!r.isOk()) {
            log.error("pause device({}) job({}) failed ({})", group, schedule.getName(), r.getMessage());
        }
    }

    @Override
    @SneakyThrows
    public void resumeJob(String group, Schedule schedule) {
        JobKey jobKey = JobKey.jobKey(schedule.getName(), group);
        scheduler.resumeJob(jobKey);
        R<Schedule> r = scheduleClient.update(schedule.setStatus((short) 1));
        if (!r.isOk()) {
            log.error("resume device({}) job({}) failed ({})", group, schedule.getName(), r.getMessage());
        }
    }

    @SneakyThrows
    public void runJob(String group, Schedule schedule) {
        Class<? extends Job> jobClass = (Class<? extends Job>) (Class.forName(Common.Sdk.JOB_PREFIX + schedule.getBeanName()).newInstance().getClass());
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(schedule.getName(), group).build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(schedule.getName(), group)
                .startAt(DateBuilder.futureDate(1, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(CronScheduleBuilder.cronSchedule(schedule.getCornExpression()))
                .startNow().build();
        scheduler.scheduleJob(jobDetail, trigger);
        if (!scheduler.isShutdown()) {
            scheduler.start();
            R<Schedule> r = scheduleClient.update(schedule.setStatus((short) 0));
            if (!r.isOk()) {
                log.error("run device({}) job({}) failed ({})", group, schedule.getName(), r.getMessage());
            }
        }
    }

    public List<Schedule> getScheduleList(Long deviceId) {
        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setDeviceId(deviceId);
        scheduleDto.setPage(new Pages().setSize(-1L));
        R<Page<Schedule>> r = scheduleClient.list(scheduleDto);
        return r.isOk() ? r.getData().getRecords() : new ArrayList<>();
    }
}
