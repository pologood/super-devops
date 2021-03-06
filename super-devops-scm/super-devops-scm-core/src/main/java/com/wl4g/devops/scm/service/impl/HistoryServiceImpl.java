/*
 * Copyright 2015 the original author or authors.
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
package com.wl4g.devops.scm.service.impl;

import com.wl4g.devops.common.bean.scm.*;
import com.wl4g.devops.common.bean.scm.model.PreReleaseModel;
import com.wl4g.devops.common.bean.scm.model.BaseModel.ReleaseInstance;
import com.wl4g.devops.common.bean.scm.model.BaseModel.ReleaseMeta;
import com.wl4g.devops.dao.scm.AppGroupDao;
import com.wl4g.devops.dao.scm.ConfigurationDao;
import com.wl4g.devops.dao.scm.HistoryDao;
import com.wl4g.devops.scm.service.AppGroupService;
import com.wl4g.devops.scm.service.ConfigServerService;
import com.wl4g.devops.scm.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class HistoryServiceImpl implements HistoryService {

	@Autowired
	private HistoryDao historyDao;
	@Autowired
	private ConfigurationDao configGurationDao;
	@Autowired
	private AppGroupDao appGroupDao;
	@Autowired
	private ConfigServerService configServerService;
	@Autowired
	private AppGroupService appGroupService;

	@Override
	public void insert(HistoryOfDetail historyOfDetail) {
		historyOfDetail.preInsert();
		historyDao.insert(historyOfDetail);
	}

	@Override
	public void insertDetail(ReleaseDetail detail) {
		detail.preInsert();
		historyDao.insertDetail(detail);
	}

	@Override
	public boolean delete(ReleaseHistory history) {
		history.preUpdate();
		return historyDao.delete(history);
	}

	public boolean versionDelete(Version history) {
		history.preUpdate();
		return historyDao.versionDelete(history);
	}

	public boolean versionUpdate(Version history) {
		history.preUpdate();
		return historyDao.versionUpdate(history);
	}

	@Override
	public List<ReleaseHistory> select(String of_id, String of_type, String updateDate, String createDate, int status) {
		return historyDao.select(of_id, of_type, updateDate, createDate, status);
	}

	@Override
	public List<ConfigVersionList> list(ConfigVersionList agl) {
		return historyDao.list(agl);
	}

	public List<VersionList> versionList(Map<String, Object> param) {
		return historyDao.versionList(param);
	}

	@Override
	public List<ReleaseHistoryList> historylist(ReleaseHistoryList agl) {
		return historyDao.historylist(agl);
	}

	@Override
	public boolean updateHistory(ReleaseDetail detail) {
		detail.preUpdate();
		return historyDao.updateHistory(detail);
	}

	@Override
	public boolean insertReleDetail(ReleaseDetail detail) {
		detail.preInsert();
		return historyDao.insertReleDetail(detail);
	}

	@Override
	public ReleaseDetail reledetailselect(ReleaseDetail releaseDetail) {
		return historyDao.reledetailselect(releaseDetail);
	}

	@Override
	public void releaseRollback(ConfigVersionList agl) {
		agl.preInsert();
		// 添加一条历史版本
		HistoryOfDetail historyOfDetail = new HistoryOfDetail();
		Version version = new Version();
		version.setId(agl.getId());
		Version versionselect = historyDao.versionselect(version);
		if (versionselect == null) {
			throw new RuntimeException("版本不存在");
		} else if (versionselect.getDelFlag() == Version.DEL_FLAG_DELETE) {
			throw new RuntimeException("版本已删除");
		}
		historyOfDetail.setVersionid(agl.getId());
		historyOfDetail.setCreateDate(agl.getCreateDate());
		historyOfDetail.setCreateBy(agl.getCreateBy());
		historyOfDetail.setRemark(agl.getRemark());
		historyOfDetail.setType(HistoryOfDetail.type.ROLLBACK.getValue());
		historyDao.insert(historyOfDetail);
		ReleaseDetail releaseDetail = new ReleaseDetail();
		releaseDetail.setReleaseId(historyOfDetail.getId());
		releaseDetail.setInstanceId(Integer.parseInt(agl.getInstanceId()));
		releaseDetail.setResult("暂无结果");
		historyDao.insertDetail(releaseDetail);
		Map<String, Object> nMap = new HashMap<>();
		nMap.put("vid", agl.getId());
		nMap.put("nodeid", agl.getInstanceId());
		nMap.put("updateBy", agl.getUpdateBy());
		nMap.put("updateDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(agl.getUpdateDate()));
		configGurationDao.updateNode(nMap);

		// Get application group information.
		AppGroup appGroup = this.appGroupDao.getAppGroup(String.valueOf(agl.getGroupId()));
		// Get application environment information.
		String envName = this.appGroupDao.selectEnvName(String.valueOf(agl.getEnvId()));
		int versionId = agl.getId();
		// Get application nodeList information

		AppInstance appInstance = new AppInstance();
		appInstance.setGroupId(Long.parseLong(String.valueOf(agl.getGroupId())));
		appInstance.setEnvId(agl.getEnvId());
		List<AppInstance> nodeList = appGroupService.instancelist(appInstance);
		// Define release instance list.
		List<ReleaseInstance> instances = new ArrayList<>();
		for (AppInstance instance : nodeList) {
			// Get application instance information.
			ReleaseInstance releaseInstance = new ReleaseInstance();
			releaseInstance.setHost(instance.getHost());
			releaseInstance.setPort(instance.getPort());
			instances.add(releaseInstance);
		}
		PreReleaseModel preRelease = new PreReleaseModel();
		preRelease.setApplication(appGroup.getName());
		preRelease.setProfile(envName);
		ReleaseMeta meta = new ReleaseMeta(String.valueOf(historyOfDetail.getId()), String.valueOf(versionId));
		preRelease.setReleaseMeta(meta);
		preRelease.setInstances(instances);
		this.configServerService.release(preRelease);
	}
}