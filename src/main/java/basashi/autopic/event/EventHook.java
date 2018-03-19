package basashi.autopic.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import basashi.autopic.client.ClientProxy;
import basashi.autopic.config.ConfigValue;
import basashi.autopic.config.CtlConfig;
import basashi.autopic.core.ModCommon;
import basashi.autopic.core.log.ModLog;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventHook{
	public static final EventHook instance = new EventHook();
	public int modekey = 0;
	public int rgstkey = 0;
	private long packetEnableTime = 0L;
	private boolean countflag = false;
	private int changeflag=0;
	private int changeflag2=0;
	private int count;
	private static final long packetWaitMilliSec = 200L;
	private String[] itemList;

	private static final BlockingQueue<EntityPlayer> _serverPacket = new LinkedBlockingQueue();

	private EventHook(){
		reloadkye();
		itemList = null;
		if (!"".equals(ConfigValue.itemlist)){
			itemList = ConfigValue.itemlist.split(",");
		}
	}

	private void reloadkye(){
//		modekey = CtlConfig.getConfigKey(ConfigValue.ModeChangeKey);
//		rgstkey = CtlConfig.getConfigKey(ConfigValue.RegistKey);
	}

	// ピックアップイベント
	@SubscribeEvent
	public void pickUpEvent(EntityItemPickupEvent event)
	{
		ModLog.log().debug("event");
		EntityItem etitem = event.getItem();
		if (ConfigValue.DefaultMode != 0){
			if(!this.CheckPickItem(etitem)){
				etitem.setPickupDelay(100);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!TickEvent.Phase.END.equals(event.phase)) {
			return;
		}

		// ワールド情報、プレイヤー情報を取得
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		World world = minecraft.world;
		EntityPlayerSP player = minecraft.player;
		if (null == player) {
			return;
		}

		// キーコンフィグを読み直し
		if (CtlConfig.reloadConfig()) {
			reloadkye();
			itemList = null;
			if (!"".equals(ConfigValue.itemlist)){
				itemList = ConfigValue.itemlist.split(",");
			}
		}

		if (null != world) {
			if (System.currentTimeMillis() >= this.packetEnableTime) {
				if (player.getHealth() > 0.0F && ConfigValue.DefaultMode != 0 && countflag){
					// あたり判定に引っかかったアイテムを取得
					List<Entity> list = getEntitiesInCircumference(world, player);
					if(list != null){
						Packet<INetHandlerPlayServer> p = new CPacketCustomPayload(ModCommon.CHANEL_NAME,
								new PacketBuffer(Unpooled.wrappedBuffer(new byte[1])));

						minecraft.getConnection().sendPacket(p);
						NetworkDispatcher disp = NetworkDispatcher.get(player.connection.getNetworkManager());
						this.packetEnableTime = (System.currentTimeMillis() + 200L);
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
		if (ClientProxy.Press_Key_Enable()
				&& (null == minecraft.currentScreen)) {
			if (this.changeflag <= 0){
				ConfigValue.DefaultMode = ((ConfigValue.DefaultMode++)>1)?0:ConfigValue.DefaultMode++;
				player.sendStatusMessage(new TextComponentString("AutoPic " + ((ConfigValue.DefaultMode==0) ? "OFF" :((ConfigValue.DefaultMode==1)? "Ignore List": "Allow List"))),true);
				this.changeflag = 2;
			}else if (this.changeflag >= 1){
				this.changeflag--;
			}
		}

		// モード変更キー押下
		if (ClientProxy.Press_Key_Regist()
				&& (null == minecraft.currentScreen)){
			if (this.changeflag2 <= 0){
				ItemStack itm = player.inventory.getCurrentItem();
				if ( itm != null ){
					boolean res = ConfigValue.AddOrRemoveItem(itm.getItem().getRegistryName().toString());
					CtlConfig.saveConfig();
					itemList = null;
					if (!"".equals(ConfigValue.itemlist)){
						itemList = ConfigValue.itemlist.split(",");
					}
					player.sendStatusMessage(new TextComponentString("AutoPic " + ((res) ? "Add List ":"Remove List")+ itm.getItem().getRegistryName()),true);
				}
				this.changeflag2 = 2;
			}else if ( this.changeflag2 >= 1){
				this.changeflag2--;
			}
		}
	}

	@SubscribeEvent
	public void onServerPacket(ServerCustomPacketEvent event) {
		//ModLog.log().debug("start");
		EntityPlayer player = ((NetHandlerPlayServer) event.getHandler()).player;
		if (null == player) {
			return;
		}
		_serverPacket.offer(player);
		//ModLog.log().debug("end");
	}

	@SubscribeEvent
	public void tickEventServer(TickEvent.ServerTickEvent event) {
		//ModLog.log().debug("start");
		if (!TickEvent.Phase.END.equals(event.phase)) {
			return;
		}
		EntityPlayer player = (EntityPlayer) _serverPacket.poll();
		if (null == player) {
			return;
		}
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server) {
			return;
		}
		World world = server.getWorld(player.dimension);
		List<Entity> list = getEntitiesInCircumference(world, player);
		if (null == list) {
			return;
		}
		startPickup(world, player, list, false);

		//ModLog.log().debug("end");
	}

	protected void startPickup(World world, EntityPlayer player, List<Entity> list, boolean isThreadClient) {
		//ModLog.log().debug("start");
		WorldServer ws = (world instanceof WorldServer) ? (WorldServer) world : null;
		for (Entity entity : list) {
			if (!entity.isDead) {
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

	protected List<Entity> getEntitiesInCircumference(World world, EntityPlayer player) {
		//ModLog.log().debug("start");
		List<Entity> ret = new ArrayList();
		if ( ConfigValue.DefaultMode != 0){
			// あたり判定に引っかかったエンティティを取得
			List<?> list = world.getEntitiesWithinAABBExcludingEntity(player,
					player.getEntityBoundingBox().expand(ConfigValue.horizontal, ConfigValue.vertical, ConfigValue.horizontal).expand(-1*ConfigValue.horizontal, -1*ConfigValue.vertical, -1*ConfigValue.horizontal));
			if ((null == list) || (list.isEmpty())) {
				return null;
			}

			for (Object o : list) {
				Entity e = (Entity) o;
				if (!e.isDead) {
					boolean allow = false;
					if ((e instanceof EntityXPOrb)){
						// 経験値オーブは必ず拾う
						allow = true;
					}else if((e instanceof EntityItem)){
						allow = CheckPickItem((EntityItem)e);
					}
					if (allow) {
						ret.add(e);
						ModLog.log().debug("add list:" +((EntityItem) e).getItem().getItem().getRegistryName());
					}
				}
			}
		}
		//ModLog.log().debug("end");
		return ret.isEmpty() ? null : ret;
	}

	// アイテムを拾うかどうか判定する
	protected boolean CheckPickItem(EntityItem target){
		if (ConfigValue.DefaultMode == 0){
			// ModOff状態なので必ず拾う
			return true;
		}else{
			// リスト内のアイテムを無視
			if (ConfigValue.DefaultMode == 1){
				// 無視リストが空なので必ず拾う
				if (itemList == null){return true;}
					// 一致する場合無視、一致しない場合許可
					if(isIdInList(target.getItem().getItem().getRegistryName().toString())) return false;
					else return true;
				}
				// リスト外のアイテムを無視
			else{
				// 無視リストが空なので必ず無視
				if (itemList == null){return false;}
				// 一致しない場合無視、一致する場合許可
				if(isIdInList(target.getItem().getItem().getRegistryName().toString())) return true;
				else return false;
			}
		}
	}

	public boolean isIdInList(String itemName) {
		//ModLog.log().debug("start");
		boolean ret = false;
		for(String check : itemList){
			ModLog.log().debug(itemName + " = " +check);
			if ( itemName.equals(check)){
				ret = true;
				break;
			}
		}
		//ModLog.log().debug("end");
		return ret;
	}
}