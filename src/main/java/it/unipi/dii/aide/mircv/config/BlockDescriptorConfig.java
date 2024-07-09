package it.unipi.dii.aide.mircv.config;

public class BlockDescriptorConfig {
    private String blockDescriptorsPath;
    private boolean compressionEnabled;

    public BlockDescriptorConfig(String blockDescriptorsPath, boolean compressionEnabled) {
        this.blockDescriptorsPath = blockDescriptorsPath;
        this.compressionEnabled = compressionEnabled;
    }

    public String getBlockDescriptorsPath() {
        return blockDescriptorsPath;
    }

    public void setBlockDescriptorsPath(String blockDescriptorsPath) {
        this.blockDescriptorsPath = blockDescriptorsPath;
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    public boolean getCompressionEnabled() {
        return compressionEnabled;
    }
}
