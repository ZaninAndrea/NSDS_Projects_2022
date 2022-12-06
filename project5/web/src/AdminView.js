import "./App.css"
import { MantineProvider } from "@mantine/core"
import { Button } from "@mantine/core"
import { AppShell, Navbar, Header } from "@mantine/core"
import { IconBoxSeam, IconUser, IconCheckupList } from "@tabler/icons"
import { ThemeIcon, UnstyledButton, Group, Text } from "@mantine/core"
import React, { useState } from "react"

function AdminView() {
    const [view, setView] = useState("customer")

    return <div>Admin</div>
}

export default AdminView
