package com.oasis.sdk.pay.googleplay.utils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.TimerTask;

import org.json.JSONException;

import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;

public class GoogleBillingTimer extends TimerTask {

	private static final String TAG = GoogleBillingTimer.class.getName();
	int period = 1;
	public void setPeriod() {  
        //缩短周期，执行频率就提高  
        setDeclaredField(TimerTask.class, this, "period", (long)(Math.pow(2, period)*1000));  
        if(period == 8){
        	try {// 利用定时机会，判断userinfo是否被回收，如果被回收，从新解析
    			if(!BaseUtils.isLogin())
    				BaseUtils.parseJsonToUserinfo();			
    		} catch (Exception e) {
    		}
        	period = 1;
        }else{
        	period++;
        }
    }  
      
    //通过反射修改字段的值  
    static boolean setDeclaredField(Class<?> clazz, Object obj,  
            String name, Object value) {  
        try {  
            Field field = clazz.getDeclaredField(name);  
            field.setAccessible(true);  
            field.set(obj, value);  
            return true;  
        } catch (Exception ex) {  
            ex.printStackTrace();  
            return false;  
        }  
    }  
    
	@Override
	public void run() {

		setPeriod();
		List<Purchase> orderList = null;
		try {
			orderList = GoogleBillingUtils.getPurchasedListByStatus(GoogleBillingUtils.ORDER_STATUS_UNFINISHED);
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
		
		if(orderList == null || orderList.size() <= 0){
			BaseUtils.logDebug(TAG, "There are currently no outstanding orders.");
			return;
		}
		
		int size = orderList.size();
		for (int i = 0; i < size; i++) {
			Purchase purchase = orderList.get(i);
			try {
				int res = HttpService.instance().checkPurchaseForGoogle(purchase, GoogleBillingUtils.SEPARATE);
				switch (res) {
				case 1000000://交易成功且发钻成功
				case 1000002://该购买交易已发钻成功，因客户端未消费成功，所以重复2次验证
				case 1000006://支付完成，Google验证未通过。（如无效订单等）
					GoogleBillingUtils.updatePurchase(purchase);
					break;
					
				default:
					
					break;
				}
				
			} catch (OasisSdkException e) {// 异常不用处理，继续循环下一条
			}//end try
		}//end for
	}

}
