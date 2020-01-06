package com.focus.net;

import com.focus.util.Log;
import com.focus.util.QueneListener;

public class Consumer extends QueneListener
{
	// private java.text.DecimalFormat df;
	protected long count_packet;

	protected Consumer()
	{
		// this.df = new java.text.DecimalFormat( "0.00" );
	}

	public synchronized void close()
	{
		this.isRunning = false;
		this.notify();
	}

	/**
	 */
	public void run()
	{
		while (this.isRunning)
		{
			synchronized (this)
			{
				if (super.queueObj.isEmpty())
				{
					try
					{
						this.calculagraph = 0;
						this.isBusy = false;
						this.wait();
						this.isBusy = true;
					}
					catch (InterruptedException e)
					{
					}
				}
			}

			Packet out = null;
			//long _time = System.currentTimeMillis();
			while ((out = (Packet) this.peek()) != null)
			{
				try
				{
					if (!out.send())
					{
						Log.err(this, "Failed to send packet:" + out.toString());
					}
					else
					{
						count_packet += 1;
					}
				}
				catch (Exception e)
				{
					Log.err(this, "Failed to send packet:" + e.getMessage());
					break;
				}
				finally
				{
					out.free();
				}
			}
		}
	}
}
