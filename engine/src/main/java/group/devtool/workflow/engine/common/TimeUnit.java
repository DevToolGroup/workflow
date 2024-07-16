/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.common;

public enum TimeUnit {

	SECONDS(1000L),

	MINUTES(60 * 1000L),

	HOURS(60 * 60 * 1000L),

	DAYS(24 * 60 * 60 * 1000L),

	;

	private final Long mills;

	TimeUnit(Long mills) {
		this.mills = mills;
	}

	public Long getMills() {
		return mills;
	}
}
