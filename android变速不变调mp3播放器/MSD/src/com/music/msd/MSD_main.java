package com.music.msd;

import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import com.music.msd.R.id;
import java.util.HashMap;

import android.os.Bundle;
import android.R.integer;
import android.app.Dialog;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.widget.Toast;
import android.widget.SeekBar;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.content.Context;
import android.widget.ImageView;

public class MSD_main extends Activity {

	private MyAdapter adapter;
	
	private ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
	private ListView list;
	private int list_select = 0;

	private ImageButton btn_play;
	private ImageButton btn_pre;
	private ImageButton btn_next;
	private ImageButton btn_delete;
	private SeekBar timeBar;
	private TextView tv;
	private TextView tv_current;
	private TextView tv_play;
	private mybuttonlistener clicklistener;

	// 引用libmad库，用于解码mp3
	private Mp3Decore mp3decore = new Mp3Decore();
	// 定义变量
	private Thread mThread;
	private short[] audioBuffer;
	private AudioTrack mAudioTrack;
	private int samplerate;
	private int mAudioMinBufSize;
	private int ret;
	private boolean mThreadFlag;
	private String filePath = "/sdcard/test.mp3";
	// 当前播放时间，时间为0.1s
	private int currentTime = 0;
	private Boolean isPause = true;
	private int Playtime = 0;// mp3播放时间
	private int temp_time=0;

	private int Bitrate = 0;// MP3比特率
	private int filelastps = 0;
	private int filecurrentps = 0;

	// 定义引用soundtouch变量
	private JNISoundTouch soundtouch = new JNISoundTouch();
	private int newTemp = 0;
	private boolean IsPlay = false;

	// 定义时间显示函数
	public String DisplayTime(int time) {
		int minute = 0;
		int second = 0;
		String m_str = "";
		String s_str = "";
		second = time % 60;
		minute = time / 60;
		if (minute < 10)
			m_str = "0" + minute;
		else
			m_str = "" + minute;

		if (second < 10)
			s_str = "0" + second;
		else
			s_str = "" + second;
		return m_str + ":" + s_str;
	}

	// 定义handler变量用于更新控件
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO 接收消息并且去更新UI线程上的控件内容
			switch (msg.what) {
			case 0:
				timeBar.setMax(Playtime);
				timeBar.setProgress(currentTime / 10);
				// tv.setText(""+Bitrate+"--"+currentTime);
				tv_current.setText(DisplayTime(currentTime / 10));
				tv_play.setText(DisplayTime(Playtime));
				break;
			case 1:
				btn_play.setImageDrawable(getResources().getDrawable(
						R.drawable.ic_media_play));
				// 显示播放时间
				tv_play.setText(DisplayTime(0));
				tv_current.setText(DisplayTime(currentTime));
				Toast.makeText(getApplicationContext(), "播放结束！",
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				btn_play.setImageDrawable(getResources().getDrawable(
						R.drawable.ic_media_pause));
				break;
			case 3://添加文件窗口
				showDialog(0);
				break;
			case 4://开始播放音乐
				StartAudioPlayer();
				break;

			}
			super.handleMessage(msg);
		}
	};

	// 开启计时线程
	Thread TimeCount = new Thread(new Runnable() {
		@Override
		public void run() {

			while (true) {
				try {
					if (isPause == false) {
						Thread.sleep(100);// 线程休眠0.1s
						currentTime++;
						// 发送消息
						Message msg = new Message();
						msg.what = 0;
						msg.arg1 = currentTime;
						handler.sendMessage(msg);
					}
				} catch (Exception e) {

				}
			}

		}
	}

	);

	// 界面初始化函数
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.msd_main_layout);
		adapter = new MyAdapter(this);
		list = (ListView) findViewById(R.id.listView1);

		Map<String, Object> item1 = new HashMap<String, Object>();
		item1.put("image", R.drawable.ic_media_file);
		item1.put("title", "<-添加MP3文件->");
		item1.put("patch", "");
		mData.add(item1);
		list.setAdapter(adapter);
		
		list.setAdapter(adapter);
		//设置选中的选项		
		list.setSelection(list_select);
		
		
		// 设置选项监听。
		list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				//更改选择项
				list_select=position;
				int temp=list.getCount();
			if(temp==position+1)
			{
				handler.sendEmptyMessage(3);
			}
			else
			{
				list.setSelection(list_select);
				//重新绑定显示
				list.setAdapter(adapter);
				
				Map<String, Object> item = new HashMap<String, Object>();
				item=mData.get(list_select);
				//获取路径
				filePath=(String)item.get("patch");
				
				//停止播放器
				if (null != mThread) {
					// 停止播放
					StopAudioPlayer();
				}
				handler.sendEmptyMessage(4);				
			}
			
			}
		});

		// 设置默认位置是列表的第一个位置
		Map<String, Object> item = new HashMap<String, Object>();
		item=mData.get(list_select);
		//获取路径
		filePath=(String)item.get("patch");

		// 控件定义
		btn_next = (ImageButton) findViewById(R.id.imageButton_next);
		btn_play = (ImageButton) findViewById(R.id.imageButton_play);
		btn_pre = (ImageButton) findViewById(R.id.imageButton_pre);
		btn_delete = (ImageButton) findViewById(R.id.btn_del);
		timeBar = (SeekBar) findViewById(R.id.seekBar1);
		tv = (TextView) findViewById(R.id.text1);
		tv_current = (TextView) findViewById(R.id.txt_currentTime);
		tv_play = (TextView) findViewById(R.id.txt_playTime);

		clicklistener = new mybuttonlistener();

		// 设置监听器
		btn_pre.setOnClickListener(clicklistener);
		// 播放按钮事件
		btn_play.setOnClickListener(clicklistener);
		// 下一曲
		btn_next.setOnClickListener(clicklistener);

		btn_delete.setOnClickListener(clicklistener);
		TimeCount.start();

		timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			/**
			 * 拖动条停止拖动的时候调用
			 */
			int pro;

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

				Thread newPlayThread = new Thread(new Runnable() {

					@Override
					public void run() {

						try {
							Thread.sleep(200);
						} catch (Exception e) {
							// TODO: handle exception
						}
						// TODO Auto-generated method stub
						if (false == IsPlay) {
							isPause = false;
							IsPlay = true;
							handler.sendEmptyMessage(2);
							if (null == mThread) {
								// 获得文件相关信息
								GetFileMsg(filePath);
								// 打开文件初始化播放设置
								currentTime = pro * 10;
								filecurrentps = currentTime / 10 * Bitrate / 8;
								playerinit(filePath, filecurrentps);
							}
							// TODO Auto-generated method stub
							if (ret == -1) {
								Toast.makeText(
										getApplicationContext(),
										"Couldn't open file '" + filePath + "'",
										Toast.LENGTH_SHORT).show();
							} else {
								if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
									// 音频线程开始
									mAudioTrack.play();

								} else if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
									// 音频线程开始
									mAudioTrack.play();

								} else {
									Toast.makeText(getApplicationContext(),
											"Already in play",
											Toast.LENGTH_SHORT).show();
								}
							}
						}
						// 、、、、、、、、、、、、、、、、
					}
				});

				newPlayThread.start();
			}

			/**
			 * 拖动条开始拖动的时候调用
			 */
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

				if (null != mThread) {
					// 停止播放
					StopAudioPlayer();
				}

			}

			/**
			 * 拖动条进度改变的时候调用
			 */
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// timeBar.setProgress(progress);
				pro = progress;

			}
		});
	}
	
	// 定义点击内部事件监听器类,主要用于监听播放相关按钮
	public class mybuttonlistener implements OnClickListener {
		// 按钮点击事件函数
		public void onClick(View v) {
			switch (v.getId()) {
			// 上一曲按钮
			case R.id.imageButton_pre:

				list_select--;
				if(list_select<0)
					list_select=mData.size()-1;
				
				
				//重新绑定显示
				list.setAdapter(adapter);
				list.setSelection(list_select);
				
				Map<String, Object> item = new HashMap<String, Object>();
				item=mData.get(list_select);
				//获取路径
				filePath=(String)item.get("patch");
				
				
				//停止播放器
				if (null != mThread) {
					// 停止播放
					StopAudioPlayer();
				}
				
				//重新启动播放器				
				handler.sendEmptyMessage(4);
			
			
				break;
			// 播放按钮事件监听
			case R.id.imageButton_play:
				
				//获取路径
				filePath=(String)mData.get(list_select).get("patch");
				
				/**************************************/
				if (false == IsPlay) {
					isPause = false;
					IsPlay = true;
					btn_play.setImageDrawable(getResources().getDrawable(
							R.drawable.ic_media_pause));
					if (null == mThread) {
						// 获得文件相关信息
						GetFileMsg(filePath);
						// 显示播放时间
						tv_play.setText(DisplayTime(Playtime));
						// 打开文件初始化播放设置
						playerinit(filePath, 0);
					}
					// TODO Auto-generated method stub
					if (ret == -1) {
						Toast.makeText(getApplicationContext(),
								"Couldn't open file '" + filePath + "'",
								Toast.LENGTH_SHORT).show();
					} else {
						if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
							// 音频线程开始
							mAudioTrack.play();

						} else if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
							// 音频线程开始
							mAudioTrack.play();

						} else {
							Toast.makeText(getApplicationContext(),
									"Already in play", Toast.LENGTH_SHORT)
									.show();
						}
					}
				} else {
					IsPlay = false;
					btn_play.setImageDrawable(getResources().getDrawable(
							R.drawable.ic_media_play));
					isPause = true;
					if (ret == -1) {
						Toast.makeText(getApplicationContext(),
								"Couldn't open file '" + filePath + "'",
								Toast.LENGTH_SHORT).show();
					} else {
						if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
							mAudioTrack.pause();

						} else {
							Toast.makeText(getApplicationContext(),
									"Already stop", Toast.LENGTH_SHORT).show();

						}

					}

				}
				/****************************************/
				break;
			case R.id.imageButton_next://下一曲
			
				//下一曲
				list_select++;
				if(list_select>=mData.size())
					list_select=0;
				
				
				//重新绑定显示
				list.setAdapter(adapter);
				list.setSelection(list_select);
				
				Map<String, Object> item1 = new HashMap<String, Object>();
				item1=mData.get(list_select);
				//获取路径
				filePath=(String)item1.get("patch");
				
				
				//停止播放器
				if (null != mThread) {
					// 停止播放
					StopAudioPlayer();
				}
				
				//重新启动播放器
				//发送消息，启动播放器
				handler.sendEmptyMessage(4);
				
				break;
			case R.id.btn_del:
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						MSD_main.this);
				dialog.setIcon(R.drawable.ic_menu_delete);
				dialog.setTitle("你确定要清空列表吗？");
				dialog.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// MSD_main.this.finish();
								mData.clear();
								list.setAdapter(adapter);

							}
						});
				dialog.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						});
				dialog.create().show();
				break;

			default:
				;
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, 0, 0, "添加").setIcon(R.drawable.ic_menu__add);
		menu.add(0, 1, 1, "调节").setIcon(R.drawable.ic_menu_set);
		menu.add(0, 2, 2, "退出").setIcon(R.drawable.ic_menu_exit);
		return true;
	}

	/** 覆盖onOptionsItemSelected函数判断用户选择的标题 */
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		/** 调用一次获得选择的按钮ID */
		super.onOptionsItemSelected(menuItem);
		switch (menuItem.getItemId()) {
		case 0: // 添加
			showDialog(0);
			break;
		case 1: // 设置
			CustomDialog cd = new CustomDialog(this, R.style.typedialog);
			cd.show();

			break;
		case 2: //
			AlertDialog.Builder dialog = new AlertDialog.Builder(MSD_main.this);
			dialog.setIcon(R.drawable.ic_menu_exit);
			dialog.setTitle("你确定要离开吗？");
			dialog.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							MSD_main.this.finish();
						}
					});
			dialog.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

						}
					});
			dialog.create().show();

			break;

		}

		return true;
	}

	/**********************************************
	调节播放速度窗体类
	调节参数是newTemp，范围-50~100
	计算参数mp3播放时间Playtime：
	*******************************************/
	public class CustomDialog extends Dialog {

		private EditText editText;

		public CustomDialog(Context context) {
			super(context);
			this.setCanceledOnTouchOutside(true);

		}

		public CustomDialog(Context context, int theme) {
			super(context, theme);
			this.setCanceledOnTouchOutside(true);

		}

		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			setContentView(R.layout.dialog_layout);

			editText = (EditText) findViewById(R.id.editText1);
			editText.setText(newTemp + "");
			;

			ImageButton btnaddButton = (ImageButton) findViewById(R.id.btn_add);
			ImageButton btncutButton = (ImageButton) findViewById(R.id.btn_cut);
			btnaddButton.setOnClickListener(new Button.OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					newTemp++;
					if (newTemp > 100)
						newTemp = -50;
					//计算播放时间Playtime
					double tt=100/(100.0+newTemp)*temp_time;
					Playtime=(int)tt;
					
					editText.setText(newTemp + "");

				}
			});

			btncutButton.setOnClickListener(new Button.OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					newTemp--;
					if (newTemp < -50)
						newTemp = 100;
					double tt=100/(100.0+newTemp)*temp_time;
					Playtime=(int)tt;
					editText.setText(newTemp + "");

				}
			});

		}

	}

	/**********************************************/
	public final class ViewHolder {
		public ImageView img;
		public TextView title;
		public ImageButton view_btn;
	}

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		// 获取ListView每一行
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			
		
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater
						.inflate(R.layout.list_item_layout, null);
				holder.img = (ImageView) convertView.findViewById(R.id.img);
				holder.title = (TextView) convertView.findViewById(R.id.text1);
				holder.view_btn = (ImageButton) convertView
						.findViewById(R.id.view_btn);

				//更改添加按钮的背景图片
				if(mData.get(position).get("patch")=="")
				{
					holder.view_btn.setImageDrawable(getResources().getDrawable(
							R.drawable.ic_menu__add));
				}
				// 每个按钮设置位置position为额外数据作为区分
				holder.view_btn.setTag(position);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();

			}
			holder.img.setBackgroundResource((Integer) mData.get(position).get(
					"image"));
			holder.title.setText((String) mData.get(position).get("title"));
			
			
			// 设置默认选择项为第一项，颜色为选择的灰色，其余为白色
			if (position == list_select)
				convertView.setBackgroundColor(Color.argb(50, 128, 128, 128));
			else {
				convertView.setBackgroundColor(Color.WHITE);
			}

			holder.view_btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					// 取得按钮的额外数据判断是哪个按钮被按下
					int ii = (Integer) arg0.getTag();
					if(ii+1==list.getCount())
					{
						//显示添加文件窗口
						handler.sendEmptyMessage(3);
					}
					else
					{
					// 删除数据
					mData.remove(ii);
					// 重新绑定
					list.setAdapter(adapter);
					}
					
				}
			});
			return convertView;
		}
	}

	/***************************************/
	@Override
	protected Dialog onCreateDialog(int id) {

			Map<String, Integer> images = new HashMap<String, Integer>();
			 // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹  
			images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);	// 根目录图标
			images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);	//返回上一层的图标  
			images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);	//文件夹图标 
			images.put("wav", R.drawable.ic_media_file);	 //文件图标  
			images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
			Dialog dialog = OpenFileDialog.createDialog(0, this, "打开文件", new CallbackBundle() {
				@Override
				public void callback(Bundle bundle) {
					
					mData.remove(mData.size()-1);
					String filepath = bundle.getString("path");
														
					Map<String, Object> item = new HashMap<String, Object>();
					item.put("image", R.drawable.ic_media_file);
					String title= filepath.substring(filepath.lastIndexOf("/")+1);
					item.put("title", title);
					item.put("patch", filepath);
					mData.add(item);
					
					Map<String, Object> item1 = new HashMap<String, Object>();
					item1.put("image", R.drawable.ic_media_file);
					item1.put("title", "<-添加MP3文件->");
					item1.put("patch", "");
					mData.add(item1);
					
					
					list.setAdapter(adapter);
					

				}
			}, 
			".mp3;",
			images);
			return dialog;
	}
	

	/*************************************/
	// 获得文件相关信息
	private void GetFileMsg(String file) {
		ret = mp3decore.initAudioPlayer(file, 0);
		if (ret == -1) {

		} else {
			// 获取播放时间
			Playtime = mp3decore.getAudiotime();
			temp_time=Playtime;
			// 获得文件大小
			//filesize = mp3decore.getAduioSize();
			// 获得比特率
			Bitrate = mp3decore.getAduioBitrate();
			mp3decore.closeAduioFile();
		}
	}

	// 播放初始化
	private void playerinit(String file, long start) {
		// 打开sd卡内的音乐文件
		// playtime=0;
		ret = mp3decore.initAudioPlayer(file, start);
		if (ret == -1) {

		} else {
			// 打开成功
			mThreadFlag = true;
			// 初始化播放设置
			initAudioPlayer();
			// soundtouch初始化
			soundtouch.setSampleRate(samplerate);
			soundtouch.setChannels(1);
			// setPitchSemiTones(10);
			// soundtouch.setRateChange(0);
			// 设置变速不变调取值是-50到100
			// soundtouch.setTempoChange(newTempo);
			// 定义缓冲区
			audioBuffer = new short[1024 * 1024];
			// 创建线程进行播放
			mThread = new Thread(new Runnable() {

				@Override
				public void run() {
					filelastps = 0;
					filecurrentps = 0;
					// TODO Auto-generated method stub
					while (mThreadFlag) {
						// 如果不是在暂停状态就读出数据进行播放
						if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PAUSED) {
							// ****从libmad处获取data******/
							// 获得上次文件的指针位置
							filelastps = mp3decore.getfileposition();
							// Bitrate 为MP3比特率，也就是播放1秒文件的大小
							// 计算当前音乐的播放位置
							int currenposition = (currentTime * Bitrate) / 10;
							// 默认加载大于播放1s，超过1s就不加载
							if (filelastps <= (currenposition + Bitrate)) {
								mp3decore.getAudioBuf(audioBuffer, 1024 * 4);
								// newTempo
								soundtouch.setTempoChange(newTemp);
								// 数据使用soundtouch进行变速处理
								soundtouch.putSamples(audioBuffer, 1024 * 4);
								short[] buffer;
								buffer = soundtouch.receiveSamples();
								// AudioTrack.write在16位下只能写入偶数个数据，否则会陷入死循环
								if ((buffer.length) % 2 == 1)
									mAudioTrack.write(buffer, 0,
											buffer.length - 1);
								else
									mAudioTrack.write(buffer, 0, buffer.length);
							}
							// 播放结束
							if (((Playtime - 1) * 10) <= currentTime) {
								// 停止播放
								StopAudioPlayer();
								handler.sendEmptyMessage(1);
							}

						} else {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}

				}
			});
			// 启动线程
			mThread.start();

		}

	}

	// 初始化audio播放
	private void initAudioPlayer() {
		// TODO Auto-generated method stub
		samplerate = mp3decore.getAudioSamplerate();

		samplerate = samplerate / 2;
		// 获得最小缓冲大小
		mAudioMinBufSize = AudioTrack.getMinBufferSize(samplerate, // mp3文件采样频率/2=声音的采样频率，也就是每秒有多少个采样点
				AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

		//System.out.printf("mAudioMinBufSize=%d\n", mAudioMinBufSize);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, // 指定在流的类型
				samplerate,// 设置音频数据的采样率
				AudioFormat.CHANNEL_OUT_STEREO,// 设置输出声道为双声道立体声
				AudioFormat.ENCODING_PCM_16BIT,// 设置音频数据块是8位还是16位
				mAudioMinBufSize, AudioTrack.MODE_STREAM);// 设置模式类型，在这里设置为流类型
	}

	//开始播放
	private void StartAudioPlayer()
	{
			isPause = false;
			IsPlay = true;
			btn_play.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_pause));
			if (null == mThread) {
				// 获得文件相关信息
				GetFileMsg(filePath);
				// 显示播放时间
				tv_play.setText(DisplayTime(Playtime));
				// 打开文件初始化播放设置
				playerinit(filePath, 0);
			}
			// TODO Auto-generated method stub
			if (ret == -1) {
				Toast.makeText(getApplicationContext(),
						"Couldn't open file '" + filePath + "'",
						Toast.LENGTH_SHORT).show();
			} else {
				if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
					// 音频线程开始
					mAudioTrack.play();

				} else if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
					// 音频线程开始
					mAudioTrack.play();

				} else {
					Toast.makeText(getApplicationContext(),
							"Already in play", Toast.LENGTH_SHORT)
							.show();
				}
			}
		
	}
	
	// 停止播放
	private void StopAudioPlayer() {
		mThreadFlag = false;// 音频
		mThread.interrupt();
		mp3decore.closeAduioFile();
		mAudioTrack.stop();
		//程序如果没有退出不要释放资源，要不然容易出问题，闪退
		//mAudioTrack.release();// 关闭并释放资源
		mThread = null;
		IsPlay = false;
		isPause = true;
		currentTime = 0;
	}

	// 程序退出执行的函数，销毁进程，释放资源
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		try {
			mThreadFlag = false;// 音频
			mThread.interrupt();
			mp3decore.closeAduioFile();
			mAudioTrack.stop();
			mAudioTrack.release();// 关闭并释放资源
			mThread = null;
		} catch (Exception e) {

		}
	}

}
