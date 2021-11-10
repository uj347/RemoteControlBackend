package remotecontrolbackend.Utils

class CommandPromise() {
    enum class State {
        UNCOMPLETED, SUCCESS, FAILURE
    }


    private var _triggered: Boolean = false
        @Synchronized
        set(value) {
            field = value
        }

    val triggered
        @Synchronized
        get() = _triggered


    private var _state = State.UNCOMPLETED
        @Synchronized
        set(value) {
            field = value
        }


    val state: State
        @Synchronized
        get() = _state


    @Synchronized
    fun setSuccess() {
        if (!_triggered) {
            this._state = State.SUCCESS
            _triggered = true
        } else {
            throw IllegalStateException("You trying to trigger already triggered promise")
        }
    }

    @Synchronized
    fun setFailure() {
        if (!_triggered) {
            this._state = State.FAILURE
            _triggered = true
        } else {
            throw IllegalStateException("You trying to trigger already triggered  promise")
        }
    }

    @Synchronized
            /** Returns true if this changed some state, false - if it was empty call on already rechrged Promise */
    fun recharge(): Boolean {
        if (_triggered) {
            _state = State.UNCOMPLETED
            _triggered = false
        }
        return false
    }
}