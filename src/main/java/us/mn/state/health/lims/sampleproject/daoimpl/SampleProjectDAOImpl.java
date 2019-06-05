/**
* The contents of this file are subject to the Mozilla Public License
* Version 1.1 (the "License"); you may not use this file except in
* compliance with the License. You may obtain a copy of the License at
* http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
* License for the specific language governing rights and limitations under
* the License.
*
* The Original Code is OpenELIS code.
*
* Copyright (C) The Minnesota Department of Health.  All Rights Reserved.
*
* Contributor(s): CIRG, University of Washington, Seattle WA.
*/
package us.mn.state.health.lims.sampleproject.daoimpl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import us.mn.state.health.lims.audittrail.dao.AuditTrailDAO;
import us.mn.state.health.lims.common.action.IActionConstants;
import us.mn.state.health.lims.common.daoimpl.BaseDAOImpl;
import us.mn.state.health.lims.common.exception.LIMSRuntimeException;
import us.mn.state.health.lims.common.log.LogEvent;
import us.mn.state.health.lims.hibernate.HibernateUtil;
import us.mn.state.health.lims.sampleproject.dao.SampleProjectDAO;
import us.mn.state.health.lims.sampleproject.valueholder.SampleProject;

/**
 * $Header$
 *
 * @author Hung Nguyen
 * @date created 08/04/2006
 * @version $Revision$
 */
@Component
@Transactional
public class SampleProjectDAOImpl extends BaseDAOImpl<SampleProject, String> implements SampleProjectDAO {

	@Autowired
	AuditTrailDAO auditDAO;

	public SampleProjectDAOImpl() {
		super(SampleProject.class);
	}

	@Override
	public void deleteData(List sampleProjs) throws LIMSRuntimeException {
		// add to audit trail
		try {

			for (int i = 0; i < sampleProjs.size(); i++) {
				SampleProject data = (SampleProject) sampleProjs.get(i);

				SampleProject oldData = readSampleProject(data.getId());
				SampleProject newData = new SampleProject();

				String sysUserId = data.getSysUserId();
				String event = IActionConstants.AUDIT_TRAIL_DELETE;
				// bugzilla 2104 change to SAMPLE_PROJECTS instead of SAMPLE_PROJECT
				String tableName = "SAMPLE_PROJECTS";
				auditDAO.saveHistory(newData, oldData, sysUserId, event, tableName);
			}
		} catch (Exception e) {
			// bugzilla 2154
			LogEvent.logError("SampleProjectDAOImpl", "AuditTrail deleteData()", e.toString());
			throw new LIMSRuntimeException("Error in SampleProject AuditTrail deleteData()", e);
		}

		try {
			for (int i = 0; i < sampleProjs.size(); i++) {
				SampleProject data = (SampleProject) sampleProjs.get(i);
				// bugzilla 2206
				data = readSampleProject(data.getId());
				sessionFactory.getCurrentSession().delete(data);
				// sessionFactory.getCurrentSession().flush(); // CSL remove old
				// sessionFactory.getCurrentSession().clear(); // CSL remove old
			}
		} catch (Exception e) {
			// bugzilla 2154
			LogEvent.logError("SampleProjectDAOImpl", "deleteData()", e.toString());
			throw new LIMSRuntimeException("Error in SampleProject deleteData()", e);
		}
	}

	@Override
	public boolean insertData(SampleProject sampleProj) throws LIMSRuntimeException {

		try {
			String id = (String) sessionFactory.getCurrentSession().save(sampleProj);
			sampleProj.setId(id);

			// bugzilla 1824 inserts will be logged in history table

			String sysUserId = sampleProj.getSysUserId();
			// bugzilla 2104 change to SAMPLE_PROJECTS instead of SAMPLE_PROJECT
			String tableName = "SAMPLE_PROJECTS";
			auditDAO.saveNewHistory(sampleProj, sysUserId, tableName);

			// sessionFactory.getCurrentSession().flush(); // CSL remove old
			// sessionFactory.getCurrentSession().clear(); // CSL remove old

		} catch (Exception e) {
			// bugzilla 2154
			LogEvent.logError("SampleProjectDAOImpl", "insertData()", e.toString());
			throw new LIMSRuntimeException("Error in SampleProject insertData()", e);
		}

		return true;
	}

	@Override
	public void updateData(SampleProject sampleProj) throws LIMSRuntimeException {

		SampleProject oldData = readSampleProject(sampleProj.getId());
		SampleProject newData = sampleProj;

		// add to audit trail
		try {

			String sysUserId = sampleProj.getSysUserId();
			String event = IActionConstants.AUDIT_TRAIL_UPDATE;
			// bugzilla 2104 change to SAMPLE_PROJECTS instead of SAMPLE_PROJECT
			String tableName = "SAMPLE_PROJECTS";
			auditDAO.saveHistory(newData, oldData, sysUserId, event, tableName);
		} catch (Exception e) {
			// bugzilla 2154
			LogEvent.logError("SampleProjectDAOImpl", "AuditTrail updateData()", e.toString());
			throw new LIMSRuntimeException("Error in SampleProject AuditTrail updateData()", e);
		}

		try {
			sessionFactory.getCurrentSession().merge(sampleProj);
			// sessionFactory.getCurrentSession().flush(); // CSL remove old
			// sessionFactory.getCurrentSession().clear(); // CSL remove old
			// sessionFactory.getCurrentSession().evict // CSL remove old(sampleProj);
			// sessionFactory.getCurrentSession().refresh // CSL remove old(sampleProj);
		} catch (Exception e) {
			// bugzilla 2154
			LogEvent.logError("SampleProjectDAOImpl", "updateData()", e.toString());
			throw new LIMSRuntimeException("Error in SampleProject updateData()", e);
		}
	}

	@Override
	public void getData(SampleProject sampleProj) throws LIMSRuntimeException {
		try {
			SampleProject data = sessionFactory.getCurrentSession().get(SampleProject.class, sampleProj.getId());
			// sessionFactory.getCurrentSession().flush(); // CSL remove old
			// sessionFactory.getCurrentSession().clear(); // CSL remove old
			if (data != null) {
				PropertyUtils.copyProperties(sampleProj, data);
			} else {
				sampleProj.setId(null);
			}
		} catch (Exception e) {
			// bugzilla 2154
			LogEvent.logError("SampleProjectDAOImpl", "getData()", e.toString());
			throw new LIMSRuntimeException("Error in SampleProject getData()", e);
		}
	}

	public SampleProject readSampleProject(String idString) {
		SampleProject sp = null;
		try {
			sp = sessionFactory.getCurrentSession().get(SampleProject.class, idString);
			// sessionFactory.getCurrentSession().flush(); // CSL remove old
			// sessionFactory.getCurrentSession().clear(); // CSL remove old
		} catch (Exception e) {
			// bugzilla 2154
			LogEvent.logError("SampleProjectDAOImpl", "readSampleProject()", e.toString());
			throw new LIMSRuntimeException("Error in SampleProject readSampleProject()", e);
		}

		return sp;
	}

	// AIS - bugzilla 1851
	// Diane - bugzilla 1920
	@Override
	public List getSampleProjectsByProjId(String projId) throws LIMSRuntimeException {
		List sampleProjects = new ArrayList();

		try {
			String sql = "from SampleProject sp where sp.project = :param";
			Query query = sessionFactory.getCurrentSession().createQuery(sql);
			query.setParameter("param", projId);

			sampleProjects = query.list();
			// sessionFactory.getCurrentSession().flush(); // CSL remove old
			// sessionFactory.getCurrentSession().clear(); // CSL remove old

			return sampleProjects;

		} catch (Exception e) {
			// bugzilla 2154
			LogEvent.logError("SampleProjectDAOImpl", "getSampleProjectsByProjId()", e.toString());
			throw new LIMSRuntimeException("Error in SampleProjectDAO getSampleProjectsByProjId()", e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public SampleProject getSampleProjectBySampleId(String id) throws LIMSRuntimeException {
		List<SampleProject> sampleProjects = null;

		try {
			String sql = "from SampleProject sp where sp.sample.id = :sampleId";
			Query query = sessionFactory.getCurrentSession().createQuery(sql);
			query.setInteger("sampleId", Integer.parseInt(id));

			sampleProjects = query.list();
			// closeSession(); // CSL remove old

		} catch (Exception e) {
			handleException(e, "getSampleProjectBySampleId");
		}

		return sampleProjects.isEmpty() ? null : sampleProjects.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SampleProject> getByOrganizationProjectAndReceivedOnRange(String organizationId, String projectName,
			Date lowReceivedDate, Date highReceivedDate) throws LIMSRuntimeException {
		List<SampleProject> list = null;
		try {
			String sql = "FROM SampleProject as sp "
					+ " WHERE sp.project.projectName = :projectName AND sp.sample.id IN (SELECT so.sample.id FROM SampleOrganization as so WHERE so.sample.receivedTimestamp >= :dateLow AND so.sample.receivedTimestamp <= :dateHigh "
					+ " AND   so.organization.id = :organizationId ) ";
			Query query = sessionFactory.getCurrentSession().createQuery(sql);

			query.setString("projectName", projectName);
			query.setDate("dateLow", lowReceivedDate);
			query.setDate("dateHigh", highReceivedDate);
			query.setInteger("organizationId", Integer.valueOf(organizationId));
			list = query.list();
		} catch (Exception e) {
			LogEvent.logError("SampleDAOImpl", "getSamplesByOrganiztionAndReceivedOnRange()", e.toString());
			throw new LIMSRuntimeException(
					"Exception occurred in SampleNumberDAOImpl.getByOrganizationProjectAndReceivedOnRange", e);
		}

		return list;
	}

}