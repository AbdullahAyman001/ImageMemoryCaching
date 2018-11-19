package com.tps.cacheutilrequests

import android.graphics.Bitmap
import android.widget.ImageView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.BitmapRequestListener
import com.rx2androidnetworking.Rx2AndroidNetworking
import com.tps.cacheutilrequests.CacheUtili.BitmapCache
import com.tps.cacheutilrequests.CacheUtili.BitmapObject
import com.tps.cacheutilrequests.CacheUtili.RequestOwner
import com.tps.cacheutilrequests.CacheUtili.RequestQueue

class BitmapImageCaching private constructor() {
    private var uRl: String? = null
    private var imageRef: ImageView? = null
    private var cacheEnabled: Boolean? = false

    fun loadUrl(url: String): BitmapImageCaching {
        this.uRl = url
        return this
    }

    fun setImageRef(image: ImageView): BitmapImageCaching {
        this.imageRef = image
        return this
    }

    fun getImageRef(): ImageView? {
        return this.imageRef
    }

    fun allowCaching(value: Boolean): BitmapImageCaching {
        cacheEnabled = value
        if (value) {
            val maxCacheMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt() / 4
            if (bitmapCache == null)
                bitmapCache = BitmapCache.BitmapCacheBuilder().setMaxCacheMemory(maxCacheMemory).build()
        } else {
            bitmapCache = null
        }
        return this
    }

    fun cancelRequest(url: String, owner: ImageView) {
        val ownerList = imageViewBitmapRequestQueue?.getOwnerList(url!!)
        System.out.println("Request cancelling! " + url!!)
        if (ownerList != null)
            for (ref in ownerList) {
                if (ref.equals(RequestOwner<ImageView>(getImageRef()!!))) {
                    System.out.println("Request canceled! " + url!!)
                    ownerList.remove(ref)
                    System.out.println("Request canceled! " + url!!)
                }
            }
        if (ownerList?.size == 0)
            AndroidNetworking.forceCancel(url)
    }

    @Synchronized
    fun execute() {
        if (bitmapCache != null) {
            val bitmap = bitmapCache!!.getItemBitmap(uRl!!)
            if (bitmap != null) {
                getImageRef()!!.setImageBitmap(bitmap)
                return
            }
        }
        //TODO load file
        if (imageViewBitmapRequestQueue!!.getOwnerList(uRl!!) == null) {
            imageViewBitmapRequestQueue?.addToRequestQueue(uRl!!, RequestOwner<ImageView>(getImageRef()!!))

            Rx2AndroidNetworking.get(uRl)
                .setTag(uRl)
                .build()
                .getAsBitmap(object : BitmapRequestListener {
                    override fun onResponse(response: Bitmap) {
                        val ownerList = imageViewBitmapRequestQueue?.getOwnerList(uRl!!)
                        if (ownerList != null)
                            for (ref in ownerList) {
                                ref._Ref.setImageBitmap(response)
                            }
                        else
                            getImageRef()!!.setImageBitmap(response)
                        if (cacheEnabled!!) {
                            val bitmapObject = BitmapObject.Builder()
                                .setUrl(uRl!!)
                                .setBitmap(response)
                                .setAlteredDate()
                                .create()
                            bitmapCache!!.addItemToCache(bitmapObject)

                        }
                        imageViewBitmapRequestQueue!!.removeFromRequestQueue(uRl!!)
                    }

                    override fun onError(anError: ANError) {
                        imageViewBitmapRequestQueue!!.removeFromRequestQueue(uRl!!)

                    }
                })
        }
    }

    companion object {
        private var imageViewBitmapRequestQueue: RequestQueue<ImageView>? = null
        private var instance: BitmapImageCaching? = null
        private var bitmapCache: BitmapCache? = null

        fun getInstance(): BitmapImageCaching {
            if (instance == null) {
                synchronized(BitmapImageCaching::class.java) {
                    if (instance == null) {
                        instance = BitmapImageCaching()
                    }
                }
                instance = BitmapImageCaching()
                bitmapCache = BitmapCache.BitmapCacheBuilder().build()
                imageViewBitmapRequestQueue = RequestQueue()
            }
            return instance!!
        }
    }
}
