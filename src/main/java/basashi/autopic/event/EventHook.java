package basashi.autopic.event;

import java.util.ArrayList;
import java.util.List;

import basashi.autopic.config.ConfigValue;
import basashi.autopic.config.CtlConfig;
import basashi.autopic.core.CommonProxy;
import basashi.autopic.core.log.ModLog;
import basashi.autopic.network.MessageHundler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventHook{
	public static final EventHook instance = new EventHook();
	public int modekey = 0;
	public int rgstkey = 0;
	private long packetEnableTime = 0L;
	private boolean countflag = false;
	private int changeflag=0;
	private int changeflag2=0;
	private int count;
	private String[] itemList;

	private static EntityPlayer player = null;

	private EventHook(){
		reloadkye();
		itemList = null;

		if (!"".equals(ConfigValue.ItemLists())){
			itemList = ConfigValue.ItemLists().split(",");
		}
	}

	public static void setEntityPlayer(EntityPlayer pl){
		player = pl;
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
		if (ConfigValue.DefaultMode() != 0){
			if(!this.CheckPickItem(etitem)){
				etitem.setPickupDelay(100);
			}
		}
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
			return;
		}

		// キーコンフィグを読み直し
		if (CtlConfig.reloadConfig()) {
			reloadkye();
			itemList = null;
			if (!"".equals(ConfigValue.ItemLists())){
				itemList = ConfigValue.ItemLists().split(",");
			}
		}

		if (null != world) {
			if (System.currentTimeMillis() >= this.packetEnableTime) {
				if (player.getHealth() > 0.0F && ConfigValue.GENERAL.DefaultMode.get() != 0 && countflag){
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
				if (ConfigValue.DefaultMode() == 0){
					ConfigValue.setDefaultMode(1);
				}else{
					ConfigValue.setDefaultMode(0);
				}
				this.changeflag = 2;
			}else if (this.changeflag >= 1){
				this.changeflag--;
			}
		}

		// モード変更キー押下
		if (CommonProxy.Client.Press_Key_Regist()
				&& (null == minecraft.currentScreen)){
			if (this.changeflag2 <= 0){
				ItemStack itm = player.inventory.getCurrentItem();
				if ( itm != null ){
					boolean res = ConfigValue.GENERAL.AddOrRemoveItem(itm.getItem().getRegistryName());
					itemList = null;
					if (!"".equals(ConfigValue.ItemLists())){
						itemList = ConfigValue.ItemLists().split(",");
					}
					player.sendStatusMessage(new TextComponentString("AutoPic " + ((res) ? "Add List ":"Remove List")+ itm.getItem().getRegistryName()),false);
				}
				this.changeflag2 = 2;
			}else if ( this.changeflag2 >= 1){
				this.changeflag2--;
			}
		}
	}

//	@SubscribeEvent
//	public void onServerPacket(ServerCustomPacketEvent event) {
//		//ModLog.log().debug("start");
//		EntityPlayer player = ((NetHandlerPlayServer) event.getHandler()).player;
//		if (null == player) {
//			return;
//		}
//		_serverPacket.offer(player);
//		//ModLog.log().debug("end");
//	}

	@SubscribeEvent
	public void tickEventServer(TickEvent.ServerTickEvent event) {
		//ModLog.log().debug("start");
		if (!TickEvent.Phase.END.equals(event.phase)) {
			return;
		}
		if (null == player) {
			return;
		}
		MinecraftServer server = player.getServer();
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


	protected List<Entity> getEntitiesInCircumference(World world, EntityPlayer player) {
		//ModLog.log().debug("start");
		List<Entity> ret = new ArrayList<Entity>();
		if ( ConfigValue.DefaultMode() != 0){
			// あたり判定に引っかかったエンティティを取得
			List<?> list = world.getEntitiesWithinAABBExcludingEntity(player,
					player.getBoundingBox().expand(ConfigValue.Horizontal(),
							ConfigValue.Vertical(),
							ConfigValue.Horizontal()).expand(-1*ConfigValue.Horizontal(),
									-1*ConfigValue.Vertical(), -1*ConfigValue.Horizontal()));
			if ((null == list) || (list.isEmpty())) {
				return null;
			}

			for (Object o : list) {
				Entity e = (Entity) o;
				if (!e.removed) {
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
		if (ConfigValue.DefaultMode() == 0){
			// ModOff状態なので必ず拾う
			return true;
		}else{
			// リスト内のアイテムを無視
			if (ConfigValue.DefaultMode() == 1){
				// 無視リストが空なので必ず拾う
				if (itemList == null){return true;}
					// 一致する場合無視、一致しない場合許可
				if(isIdInList(target.getItem().getItem().getRegistryName().toString())) return false;
				else return true;
				// リスト外のアイテムを無視
			}else{
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