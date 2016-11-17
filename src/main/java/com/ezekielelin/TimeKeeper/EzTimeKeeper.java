package com.ezekielelin.TimeKeeper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;


@Mod(modid = EzTimeKeeper.MODID, name = EzTimeKeeper.NAME, version = EzTimeKeeper.VERSION, acceptableRemoteVersions="*")
public class EzTimeKeeper {
	public static final String MODID = "EzTimeKeeper";
	public static final String NAME = "Time Keeper";
	public static final String VERSION = "2.1.3";

	public static final TimeZone tz = Calendar.getInstance().getTimeZone();
	
	public static double LATITUDE = 43.70437929822373;
	public static double LONGITUDE = -72.27336766415073;
		
	public static boolean ENABLED = true;
	
	private static Integer lastTime = null;
	private static int counter = 2400;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.init(event.getSuggestedConfigurationFile());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new TimeEventHandler());
	}

	public static void timeKeep(World world) {
		counter++;
		
		if (world.isRemote || !ENABLED)
			return;

		if (counter < 100 && lastTime != null) {
			world.setWorldTime(lastTime);
			return;
		} else {
			counter = 0;
		}
		
		GameRules gr = world.getGameRules();
		if (gr.getBoolean("doDaylightCycle"))
			gr.setOrCreateGameRule("doDaylightCycle", "false");
		
		Location l = new Location(LATITUDE, LONGITUDE);
		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(l, tz.getID());
		
		Calendar nowCal = GregorianCalendar.getInstance();
		Calendar sunriseCal = calculator.getOfficialSunriseCalendarForDate(GregorianCalendar.getInstance());
		Calendar sunsetCal = calculator.getOfficialSunsetCalendarForDate(GregorianCalendar.getInstance());
		
		double sunrise = (double) (sunriseCal.get(Calendar.HOUR_OF_DAY) + ((double) sunriseCal.get(Calendar.MINUTE)) / 60);
		double sunset = (double) (sunsetCal.get(Calendar.HOUR_OF_DAY) + ((double) sunsetCal.get(Calendar.MINUTE)) / 60);
		double now = (double) (nowCal.get(Calendar.HOUR_OF_DAY) + ((double) nowCal.get(Calendar.MINUTE)) / 60);
		
		int mcTime;
		
		if (now < sunrise || now > sunset) {
			double nightLength = (24 - sunset) + (sunrise);
			double percentThroughNight = 0.0;
			if (now > sunset) {
				percentThroughNight = (now - sunset) / (nightLength);
			} else {
				percentThroughNight = (now + (24 - sunset)) / (nightLength);
			}
			mcTime = (int) (12000 + (12000 * percentThroughNight));
		} else {
			double dayLength = sunset - sunrise;
			
			double percentThroughDay = (now - sunrise) / (sunset - sunrise);
			
			mcTime = (int) (12000 * percentThroughDay);
		}
		lastTime = mcTime;
		world.setWorldTime(mcTime);
	}

}
