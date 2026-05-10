package com.lonx.lyrico.data.editfield

enum class EditFieldScope {
    SingleEdit,
    BatchEdit,
    Both;

    fun supports(scene: EditFieldScene): Boolean {
        return when (this) {
            SingleEdit -> scene == EditFieldScene.SingleEdit
            BatchEdit -> scene == EditFieldScene.BatchEdit
            Both -> true
        }
    }
}
