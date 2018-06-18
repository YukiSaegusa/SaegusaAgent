package com.gmail.saegusa41010.aiwolf;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

public class AgentInfo extends SaegusaBase {

	public Role co ;
	public int wolfscore ;
	public int id ;

	public AgentInfo(Agent agent) {

		this.co =  comingoutMap.get(agent);
		this.wolfscore = 0;
		this.id = agent.getAgentIdx();
	}

}
