package com.example.space.ChatApp.ui.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.space.ChatApp.R;
import com.example.space.ChatApp.data.StaticConfig;
import com.example.space.ChatApp.models.User;
import com.example.space.ChatApp.ui.activities.AllUsersActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListAllUserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<User> users;
    private List<String> friendsId, requestSentId, requestReceivedId;
    private Context context;
    private DatabaseReference friendRequestReference;
    private DatabaseReference notificationsReference;
    private DatabaseReference friendsReference;
    private DatabaseReference messageReference;
    private String senderUid;

    public ListAllUserAdapter(Context context, List<User> users, List<String> firendsId, List<String> requestSentId, List<String> requestReceivedId, String currentUid) {
        this.context = context;
        this.users = users;
        this.friendsId = firendsId;
        this.requestSentId = requestSentId;
        this.requestReceivedId = requestReceivedId;
        this.senderUid = currentUid;

        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("friend_request");
        friendRequestReference.keepSynced(true);
        notificationsReference = FirebaseDatabase.getInstance().getReference().child("notifications");
        notificationsReference.keepSynced(true);
        friendsReference = FirebaseDatabase.getInstance().getReference().child("friends");
        friendsReference.keepSynced(true);
        messageReference = FirebaseDatabase.getInstance().getReference().child("message");
        messageReference.keepSynced(true);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //set to new item layout
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_all_user, parent, false);
        return new ItemAllUserViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final User user = users.get(position);
        final String name = user.getName();
        final String status = user.getBioText();
        final String avatar = user.getAvatar();

        // set the image
        if (avatar.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            ((ItemAllUserViewHolder) holder).imageViewProfile.setImageResource(R.drawable.default_avatar);
        } else {
            byte[] decodedString = Base64.decode(avatar, Base64.DEFAULT);
            Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ((ItemAllUserViewHolder) holder).imageViewProfile.setImageBitmap(src);
        }

        // set the name
        ((ItemAllUserViewHolder) holder).txtUsername.setText(name);

        // set the status
        ((ItemAllUserViewHolder) holder).txtStatus.setText(status);

        // set the button text and action

        if (friendsId.contains(user.getUid())) {
            ((ItemAllUserViewHolder) holder).actionBtn.setText(R.string.delete_friend);
            ((ItemAllUserViewHolder) holder).actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new android.app.AlertDialog.Builder(context)
                            .setTitle("UnFriend")
                            .setMessage("Are you sure want to unFriend " + user.getName() + " ?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // unfriend method and then change the text of button
                                    unfriendMethod(user.getUid());
                                    // update the friends list
                                    Intent intentRemoved = new Intent(AllUsersActivity.ACTION_UPDATE_LIST);
                                    intentRemoved.putExtra("type", "friends");
                                    intentRemoved.putExtra("id", user.getUid());
                                    context.sendBroadcast(intentRemoved);

                                    Intent intentRemoved2 = new Intent(AllUsersActivity.ACTION_UPDATE_LIST);
                                    intentRemoved2.putExtra("type", "received");
                                    intentRemoved2.putExtra("id", user.getUid());
                                    context.sendBroadcast(intentRemoved2);

                                    // update the text of the button
                                    ((ItemAllUserViewHolder) holder).actionBtn.setText(R.string.add_friend);
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();

                }
            });
        } else if (requestSentId.contains(user.getUid())) {
            ((ItemAllUserViewHolder) holder).actionBtn.setText(R.string.cancel_request);
            ((ItemAllUserViewHolder) holder).actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new android.app.AlertDialog.Builder(context)
                            .setTitle("Cancel request")
                            .setMessage("Are you sure want to cancel request to " + user.getName() + " ?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // cancel request method and then change the text of button
                                    cancelFriendRequestMethod(user.getUid());
                                    // update the request sent list
                                    Intent intentRemoved = new Intent(AllUsersActivity.ACTION_UPDATE_LIST);
                                    intentRemoved.putExtra("type", "sent");
                                    intentRemoved.putExtra("id", user.getUid());
                                    context.sendBroadcast(intentRemoved);
                                    // update the text of the button
                                    ((ItemAllUserViewHolder) holder).actionBtn.setText(R.string.add_friend);
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();

                }
            });
        } else if (requestReceivedId.contains(user.getUid())) {
            ((ItemAllUserViewHolder) holder).actionBtn.setText(R.string.respond);
            ((ItemAllUserViewHolder) holder).actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // un friend method and then change the text of button
                    final CharSequence[] options = {"Accept", "Delete", "Not Now"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Respond to Friend Request !");
                    builder.setIcon(R.drawable.ic_add_friend);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (options[i].equals("Accept")) {
                                acceptFriendRequestMethod(user.getUid());
                                //remove notification node
                                //for testing

                                Intent intentAdd = new Intent(AllUsersActivity.ACTION_UPDATE_LIST);
                                intentAdd.putExtra("type", "friendAdd");
                                intentAdd.putExtra("id", user.getUid());
                                context.sendBroadcast(intentAdd);
                                ((ItemAllUserViewHolder) holder).actionBtn.setText(R.string.delete_friend);
                            } else if (options[i].equals("Delete")) {
                                cancelFriendRequestMethod(user.getUid());
                                Intent intentRemoved = new Intent(AllUsersActivity.ACTION_UPDATE_LIST);
                                intentRemoved.putExtra("type", "received");
                                intentRemoved.putExtra("id", user.getUid());
                                context.sendBroadcast(intentRemoved);
                                ((ItemAllUserViewHolder) holder).actionBtn.setText(R.string.add_friend);
                            } else if (options.equals("Cancel")) {
                                dialogInterface.dismiss();
                            }
                        }
                    });
                    builder.show();
                }
            });
        } else {
            ((ItemAllUserViewHolder) holder).actionBtn.setText(R.string.add_friend);
            ((ItemAllUserViewHolder) holder).actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // add friend method and then change the text of button
                    sendFriendRequestMethod(user.getUid());
                    Intent intentAdd = new Intent(AllUsersActivity.ACTION_UPDATE_LIST);
                    intentAdd.putExtra("type", "sentAdd");
                    intentAdd.putExtra("id", user.getUid());
                    context.sendBroadcast(intentAdd);
                    // update the text of the button
                    ((ItemAllUserViewHolder) holder).actionBtn.setText(R.string.cancel_request);

                }
            });
        }
        // the remaining is the cancel request action
    }

    @Override
    public int getItemCount() {
        if (users != null) {
            return users.size();
        }
        return 0;
    }

    private void acceptFriendRequestMethod(final String receiverUid) {


        //get current date to save it as the friendship date

        Calendar friendsDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        final String saveCurrentDate = currentDate.format(friendsDate.getTime());
        //make first sender-Receiver node
        friendsReference.child(senderUid)
                .child(receiverUid)
                .setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //make Second sender-Receiver node
                        friendsReference.child(receiverUid)
                                .child(senderUid)
                                .setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //delete Friend request
                                        friendRequestReference.child(senderUid)
                                                .child(receiverUid)
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            //2-remove receiver->sender node
                                                            friendRequestReference.child(receiverUid).child(senderUid).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                //remove notification node
                                                                                notificationsReference
                                                                                        .child(receiverUid)
                                                                                        .child(senderUid)
                                                                                        .removeValue();
                                                                                notificationsReference
                                                                                        .child(senderUid)
                                                                                        .child(receiverUid)
                                                                                        .removeValue();
                                                                            }
                                                                        }
                                                                    });

                                                        }
                                                    }
                                                });

                                    }
                                });

                    }
                });


    }

    private void sendFriendRequestMethod(final String receiverUid) {
        //1-send request and create first node
        friendRequestReference.child(senderUid)
                .child(receiverUid)
                .child("request_type")
                .setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //2-tell the receiver and create second node
                            friendRequestReference.child(receiverUid)
                                    .child(senderUid)
                                    .child("request_type")
                                    .setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //notifications
                                                notificationsReference.child(receiverUid)
                                                        .child(senderUid) //will be given unique random key
                                                        .setValue("request")
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                            }
                                                        });

                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void unfriendMethod(final String receiverUid) {
        //remove first node
        friendsReference.child(senderUid).child(receiverUid).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //remove second node
                            friendsReference
                                    .child(receiverUid)
                                    .child(senderUid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            messageReference.child((senderUid + receiverUid).hashCode() + "").removeValue();
                                            messageReference.child("" + (receiverUid + senderUid).hashCode()).removeValue();
                                        }
                                    });
                        }

                    }
                });
    }

    private void cancelFriendRequestMethod(final String receiverUid) {
        //1-remove sender->receiver node
        friendRequestReference.child(senderUid)
                .child(receiverUid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //2-remove receiver->sender node
                            friendRequestReference.child(receiverUid)
                                    .child(senderUid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //remove notification node
                                                //note that here direction will be reversed

                                                notificationsReference
                                                        .child(receiverUid)
                                                        .child(senderUid)
                                                        .removeValue();
                                                notificationsReference
                                                        .child(senderUid)
                                                        .child(receiverUid)
                                                        .removeValue();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}

class ItemAllUserViewHolder extends RecyclerView.ViewHolder {
    TextView txtStatus, txtUsername;
    CircleImageView imageViewProfile;
    Button actionBtn;
    private Context context;

    ItemAllUserViewHolder(Context context, View itemView) {
        super(itemView);
        txtStatus = itemView.findViewById(R.id.all_users_status);
        txtUsername = itemView.findViewById(R.id.all_users_username);
        imageViewProfile = itemView.findViewById(R.id.all_users_profile_image);
        actionBtn = itemView.findViewById(R.id.all_user_btn);
        this.context = context;
    }
}
