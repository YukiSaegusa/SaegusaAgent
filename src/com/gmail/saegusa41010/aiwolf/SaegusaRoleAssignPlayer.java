package com.gmail.saegusa41010.aiwolf;

import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;

public class SaegusaRoleAssignPlayer extends AbstractRoleAssignPlayer {

	public SaegusaRoleAssignPlayer() {
		setVillagerPlayer(new SaegusaVillager());
		setBodyguardPlayer(new SaegusaBodyguard());
		setSeerPlayer(new SaegusaSeer());
		setPossessedPlayer(new SaegusaPossessed());
		setWerewolfPlayer(new SaegusaWereWolf());
	}

	@Override
	public String getName() {
		return "SaegusaRollAssignPlayer";
		
	}
}
