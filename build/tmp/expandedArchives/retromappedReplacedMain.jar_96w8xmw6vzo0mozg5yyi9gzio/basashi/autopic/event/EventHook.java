package basashi.autopic.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import basashi.autopic.config.ConfigValue;
import basashi.autopic.config.CtlConfig;
import basashi.autopic.core.ModCommon;
import basashi.autopic.core.log.ModLog;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
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

	private static final BlockingQueue<EntityPlayer> _serverPacket = new LinkedBlockingQueue();

	private EventHook(){
		reloadkye();
	}

	private void reloadkye(){
		modekey = CtlConfig.getConfigKey(ConfigValue.ModeChangeKey);
		rgstkey = CtlConfig.getConfigKey(ConfigValue.RegistKey);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!TickEvent.Phase.END.equals(event.phase)) {
			return;
		}

		// ワールド情報、プレイヤー情報を取得
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		World world = minecraft.field_71441_e;
		EntityPlayer player = minecraft.field_71439_g;
		if (null == player) {
			return;
		}

		// キーコンフィグを読み直し
		if (CtlConfig.reloadConfig()) {
			reloadkye();
		}

		if (null != world) {
			if (System.currentTimeMillis() >= this.packetEnableTime) {
				if (player.func_110143_aJ() > 0.0F && ConfigValue.DefaultMode != 0 && countflag){
					// あたり判定に引っかかったアイテムを取得
					List<Entity> list = getEntitiesInCircumference(world, player);
					if(list != null){
						Packet<INetHandlerPlayServer> p = new C17PacketCustomPayload(ModCommon.CHANEL_NAME,
								new PacketBuffer(Unpooled.wrappedBuffer(new byte[1])));
						minecraft.func_147114_u().func_147297_a(p);
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
		if ((this.modekey >= 1) && (org.lwjgl.input.Keyboard.isKeyDown(this.modekey))
				&& (null == minecraft.field_71462_r)) {
			if (this.changeflag <= 0){
				ConfigValue.DefaultMode = ((ConfigValue.DefaultMode++)>1)?0:ConfigValue.DefaultMode++;
				player.func_146105_b(new ChatComponentText("AutoPic " + ((ConfigValue.DefaultMode==0) ? "OFF" :((ConfigValue.DefaultMode==1)? "Ignore List": "Allow List"))));
				this.changeflag = 10;
			}else if ( this.changeflag >= 1){
				this.changeflag--;
			}
		}

		// モード変更キー押下
		if ((this.rgstkey >= 1) && (org.lwjgl.input.Keyboard.isKeyDown(this.rgstkey))
				&& (null == minecraft.field_71462_r)){
			if (this.changeflag2 <= 0){
				ItemStack itm = player.field_71071_by.func_70448_g();
				if ( itm != null ){
					ConfigValue.AddOrRemoveItem(itm.func_77973_b().getRegistryName());
					CtlConfig.saveConfig();
				}
				this.changeflag2 = 10;
			}else if ( this.changeflag2 >= 1){
				this.changeflag2--;
			}
		}
	}

	@SubscribeEvent
	public void onServerPacket(ServerCustomPacketEvent event) {
		//ModLog.log().debug("start");
		EntityPlayer player = ((NetHandlerPlayServer) event.handler).field_147369_b;
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
		World world = server.func_71218_a(player.field_71093_bK);
		List<Entity> list = getEntitiesInCircumference(world, player);
		if (null == list) {
			return;
		}
		startPickup(world, player, list, false);

		//ModLog.log().debug("end");
	}

	protected static void startPickup(World world, EntityPlayer player, List<Entity> list, boolean isThreadClient) {
		//ModLog.log().debug("start");
		WorldServer ws = (world instanceof WorldServer) ? (WorldServer) world : null;
		for (Entity entity : list) {
			if (!entity.field_70128_L) {
				if (isThreadClient) {
					entity = ws.func_73045_a(entity.func_145782_y());
					if (null == entity) {}
				} else {
					ModLog.log().debug("onCollideWithPlayer");
					entity.func_70100_b_(player);
				}
			}
		}
		//ModLog.log().debug("end");
	}

	protected static List<Entity> getEntitiesInCircumference(World world, EntityPlayer player) {
		//ModLog.log().debug("start");
		// あたり判定に引っかかったエンティティを取得
		List<?> list = world.func_72839_b(player,
				player.func_174813_aQ().func_72314_b(ConfigValue.horizontal, ConfigValue.vertical, ConfigValue.horizontal));
		if ((null == list) || (list.isEmpty())) {
			return null;
		}

		List<Entity> ret = new ArrayList();
		// アイテムリストを作る
		String[] itemList = null;
		if ( !"".equals(ConfigValue.itemlist)){
			itemList = ConfigValue.itemlist.split(",");
		}

		for (Object o : list) {
			Entity e = (Entity) o;
			if (!e.field_70128_L) {
				boolean allow = false;
				if ((e instanceof EntityXPOrb)){
					// 経験値オーブは必ず拾う
					allow = true;
				}else if((e instanceof EntityItem)){
					if (ConfigValue.DefaultMode == 1){
						// 無視リスト
						if (itemList == null){
							// リストに何もないのですべて拾う
							allow = true;
							ModLog.log().debug("all pick");
						}else{
							ItemStack itemstack = ((EntityItem) e).func_92059_d();
							// リストと合致しないものを許可する
							allow = !isIdInList(itemstack.func_77973_b().getRegistryName(), itemList);
						}
					}else if (ConfigValue.DefaultMode == 2){
						// 許可リスト
						if (itemList == null){
							// リストに何もないので、すべて拾わない
							allow = false;
							ModLog.log().debug("all nopick");
						}else{
							ItemStack itemstack = ((EntityItem) e).func_92059_d();
							// リストと合致するものを許可する
							allow = isIdInList(itemstack.func_77973_b().getRegistryName(), itemList);
						}
					}else{
						// リスト無効ナノで必ず拾う
						allow = true;
					}
				}
				if (allow) {
					ret.add(e);
					ModLog.log().debug("add list:" +((EntityItem) e).func_92059_d().func_77973_b().getRegistryName());
				}
			}
		}
		//ModLog.log().debug("end");
		return ret.isEmpty() ? null : ret;
	}

	public static boolean isIdInList(String itemName, String[] list) {
		//ModLog.log().debug("start");
		boolean ret = false;
		for(String check : list){
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