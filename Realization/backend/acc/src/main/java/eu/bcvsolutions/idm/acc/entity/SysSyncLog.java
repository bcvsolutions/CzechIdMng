package eu.bcvsolutions.idm.acc.entity;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * <i>SysSyncLog</i> is responsible for keep log informations about
 * synchronization.
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "sys_sync_log", indexes = {
		@Index(name = "idx_sys_s_l_config", columnList = "synchronization_config_id")})
public class SysSyncLog extends AbstractEntity  {

	private static final long serialVersionUID = -5447620157233410338L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "synchronization_config_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSyncConfig synchronizationConfig;

	@NotNull
	@Column(name = "running", nullable = false)
	private boolean running = false;
	
	@NotNull
	@Column(name = "contains_error", nullable = false)
	private boolean containsError = false;

	@Column(name = "started")
	private ZonedDateTime started;

	@Column(name = "ended")
	private ZonedDateTime ended;

	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "token")
	private String token;

	@OneToMany(mappedBy = "syncLog", fetch = FetchType.LAZY)
	private List<SysSyncActionLog> syncActionLogs;

	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "log")
	private String log;

	public SysSyncConfig getSynchronizationConfig() {
		return synchronizationConfig;
	}

	public void setSynchronizationConfig(SysSyncConfig synchronizationConfig) {
		this.synchronizationConfig = synchronizationConfig;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public ZonedDateTime getStarted() {
		return started;
	}

	public void setStarted(ZonedDateTime started) {
		this.started = started;
	}

	public ZonedDateTime getEnded() {
		return ended;
	}

	public void setEnded(ZonedDateTime ended) {
		this.ended = ended;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public List<SysSyncActionLog> getSyncActionLogs() {
		if (this.syncActionLogs == null) {
			this.syncActionLogs = new ArrayList<>();
		}
		return syncActionLogs;
	}

	public void setSyncActionLogs(List<SysSyncActionLog> syncActionLogs) {
		this.syncActionLogs = syncActionLogs;
	}

	public boolean isContainsError() {
		return containsError;
	}

	public void setContainsError(boolean containsError) {
		this.containsError = containsError;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}
}
