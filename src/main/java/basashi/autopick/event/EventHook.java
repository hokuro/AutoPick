package basashi.autopick.event;

import java.util.ArrayList;
import java.util.List;

import basashi.autopick.config.MyConfig;
import basashi.autopick.core.CommonProxy;
import basashi.autopick.core.log.ModLog;
import basashi.autopick.network.MessageHundler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventHook{
	public static final EventHook instance = new EventHook();
	public int modekey = 0;
	public int rgstkey = 0;
	private long packetEnableTime = 0L;
	private boolean countflag = false;
	private int changeflag=0;
	private int count;

	private static EntityPlayer entplayer = null;

	private EventHook(){
	}

	public static void setEntityPlayer(EntityPlayer pl){
		entplayer = pl;
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!TickEvent.Phase.END.equals(event.phase)) {
			return;
		}

		// ワールド情報、プレイヤー情報を取得
		Minecraft minecraft = Minecraft.getInstance();
		World world = minecraft.world;
		EntityPlayerSP player = minecraft.player;
		if (null == player) {
			entplayer = null;
			return;
		}

		if (null != world) {
			if (System.currentTimeMillis() >= this.packetEnableTime) {
				if (player.getHealth() > 0.0F && MyConfig.GENERAL.DefaultMode.get() != 0 && countflag){
					// あたり判定に引っかかったアイテムを取得
					List<Entity> list = getEntitiesInCircumference(world, player);
					if(list != null){
						MessageHundler.send_MSG_GetItem();
					}
				}
			}
		}

		// 60ticごとに拾得処理を動かす
		if (!this.countflag) {
			if (this.count > 60) {
				this.countflag = true;
			} else {
				this.count += 1;
			}
		}

		// モード変更キー押下
		if (CommonProxy.Client.Press_Key_Enable()
				&& (null == minecraft.currentScreen)) {
			if (this.changeflag <= 0){
				if (MyConfig.DefaultMode() == 0){
					MyConfig.setDefaultMode(1);
					player.sendChatMessage("enable autpick");
				}else{
					MyConfig.setDefaultMode(0);
					player.sendChatMessage("disable autpick");
				}
				this.changeflag = 2;
			}else if (this.changeflag >= 1){
				this.changeflag--;
			}
		}
	}

	@SubscribeEvent
	public void tickEventServer(TickEvent.ServerTickEvent event) {
		//ModLog.log().debug("start");
		if (!TickEvent.Phase.END.equals(event.phase)) {
			return;
		}
		if (null == entplayer) {
			return;
		}
		MinecraftServer server = entplayer.getServer();
		if (null == server) {
			return;
		}
		World world = server.getWorld(entplayer.dimension);
		List<Entity> list = getEntitiesInCircumference(world, entplayer);
		if (null == list) {
			return;
		}
		startPickup(world, entplayer, list, false);

		//ModLog.log().debug("end");
	}


	protected List<Entity> getEntitiesInCircumference(World world, EntityPlayer player) {
		//ModLog.log().debug("start");
		List<Entity> ret = new ArrayList<Entity>();
		if ( MyConfig.DefaultMode() != 0){
			// あたり判定に引っかかったエンティティを取得
			List<?> list = world.getEntitiesWithinAABBExcludingEntity(player,
					player.getBoundingBox().expand(MyConfig.Horizontal(),
							MyConfig.Vertical(),
							MyConfig.Horizontal()).expand(-1*MyConfig.Horizontal(),
									-1*MyConfig.Vertical(), -1*MyConfig.Horizontal()));
			if ((null == list) || (list.isEmpty())) {
				return null;
			}

			for (Object o : list) {
				Entity e = (Entity) o;
				if (e.isAlive()) {
					ret.add(e);
				}
			}
		}
		//ModLog.log().debug("end");
		return ret.isEmpty() ? null : ret;
	}

	protected void startPickup(World world, EntityPlayer player, List<Entity> list, boolean isThreadClient) {
		//ModLog.log().debug("start");
		WorldServer ws = (world instanceof WorldServer) ? (WorldServer) world : null;
		for (Entity entity : list) {
			if (entity.isAlive()) {
				if (isThreadClient) {
					entity = ws.getEntityByID(entity.getEntityId());
					if (null == entity) {}
				} else {
					ModLog.log().debug("onCollideWithPlayer");
					entity.onCollideWithPlayer(player);
				}
			}
		}
		//ModLog.log().debug("end");
	}
}