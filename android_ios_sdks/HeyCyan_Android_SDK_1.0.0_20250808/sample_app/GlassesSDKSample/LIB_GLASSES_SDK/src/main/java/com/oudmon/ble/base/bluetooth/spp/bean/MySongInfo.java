package com.oudmon.ble.base.bluetooth.spp.bean;
import java.util.List;

public class MySongInfo {
    public int songNameOffset;
    public int songNameLength;
    public int songIndexInFileList;
    public int relatePlayListIndex;
    public String songName;
    public String songNameWithoutSuffix;
    public byte[] songNameBuffer;
    public List<Integer> relatePlayList;
    public boolean checked;
    public boolean deleted;

    public int getSongNameOffset() {
        return songNameOffset;
    }

    public void setSongNameOffset(int songNameOffset) {
        this.songNameOffset = songNameOffset;
    }

    public int getSongNameLength() {
        return songNameLength;
    }

    public void setSongNameLength(int songNameLength) {
        this.songNameLength = songNameLength;
    }

    public int getSongIndexInFileList() {
        return songIndexInFileList;
    }

    public void setSongIndexInFileList(int songIndexInFileList) {
        this.songIndexInFileList = songIndexInFileList;
    }

    public int getRelatePlayListIndex() {
        return relatePlayListIndex;
    }

    public void setRelatePlayListIndex(int relatePlayListIndex) {
        this.relatePlayListIndex = relatePlayListIndex;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongNameWithoutSuffix() {
        return songNameWithoutSuffix;
    }

    public void setSongNameWithoutSuffix(String songNameWithoutSuffix) {
        this.songNameWithoutSuffix = songNameWithoutSuffix;
    }

    public byte[] getSongNameBuffer() {
        return songNameBuffer;
    }

    public void setSongNameBuffer(byte[] songNameBuffer) {
        this.songNameBuffer = songNameBuffer;
    }

    public List<Integer> getRelatePlayList() {
        return relatePlayList;
    }

    public void setRelatePlayList(List<Integer> relatePlayList) {
        this.relatePlayList = relatePlayList;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
