package org.eqasim.ile_de_france;

import org.eqasim.core.simulation.analysis.EqasimAnalysisModule;
import org.eqasim.core.simulation.mode_choice.EqasimModeChoiceModule;
import org.eqasim.ile_de_france.mode_choice.IDFModeChoiceModule;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.bicycle.BicycleConfigGroup;
import org.matsim.contrib.bicycle.Bicycles;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup.LinkDynamics;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;

import org.matsim.api.core.v01.population.Person;
import org.matsim.vehicles.Vehicle;
import java.util.HashMap;
import java.util.Map;

public class RunSimulation {
	static public void main(String[] args) throws ConfigurationException {
		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("config-path") //
				.allowPrefixes("mode-choice-parameter", "cost-parameter") //
				.build();

		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"), IDFConfigurator.getConfigGroups());
		cmd.applyConfiguration(config);

		config.qsim().setLinkDynamics(LinkDynamics.SeepageQ);
		config.qsim().setRestrictingSeepage(false);
		config.qsim().setSeepModeStorageFree(true);
		config.qsim().setTrafficDynamics(TrafficDynamics.queue);
		
		config.plansCalcRoute().setRoutingRandomness(3.);
		//config.plansCalcRoute().setAccessEgressType(PlansCalcRouteConfigGroup.AccessEgressType.accessEgressModeToLink);
					
		Scenario scenario = ScenarioUtils.createScenario(config);
		IDFConfigurator.configureScenario(scenario);
		ScenarioUtils.loadScenario(scenario);
		IDFConfigurator.adjustScenario(scenario);


		// if there is a vehicles file defined in config, manually assign them to their agents
		if (config.vehicles().getVehiclesFile() != null) {
			for (Person person : scenario.getPopulation().getPersons().values()) {
				Id<Vehicle> vehicleId = Id.createVehicleId(person.getId());
				Map<String, Id<Vehicle>> modeVehicle = new HashMap<>();
				modeVehicle.put("car", vehicleId);

				Id<Vehicle> vehicleId_bike = Id.createVehicleId(String.valueOf(person.getId()) + "_bike");
				modeVehicle.put("bike", vehicleId_bike);

				VehicleUtils.insertVehicleIdsIntoAttributes(person, modeVehicle);
			}
		}
		
		BicycleConfigGroup bicycleConfigGroup = (BicycleConfigGroup) config.getModules().get(BicycleConfigGroup.GROUP_NAME);

		Controler controller = new Controler(scenario);
		IDFConfigurator.configureController(controller);
		controller.addOverridingModule(new EqasimAnalysisModule());
		controller.addOverridingModule(new EqasimModeChoiceModule());
		controller.addOverridingModule(new IDFModeChoiceModule(cmd));
		Bicycles.addAsOverridingModule(controller);	
		controller.run();
	}
}