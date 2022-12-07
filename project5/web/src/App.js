import "./App.css"
import { IconBoxSeam, IconUser, IconCheckupList } from "@tabler/icons"
import {
    ThemeIcon,
    UnstyledButton,
    Group,
    Text,
    Box,
    Header,
    MantineProvider,
} from "@mantine/core"
import React, { useState } from "react"
import CustomerView from "./CustomerView"
import AdminView from "./AdminView"
import DeliveryView from "./DeliveryView"

function MainLink({ icon, color, label, selected, onClick }) {
    return (
        <UnstyledButton
            sx={(theme) => ({
                display: "block",
                padding: theme.spacing.xs,
                borderRadius: theme.radius.sm,
                color: theme.black,
                backgroundColor: selected ? theme.colors[color][2] : "",
                "&:hover": {
                    backgroundColor: selected
                        ? theme.colors[color][2]
                        : theme.colors.gray[2],
                },
            })}
            onClick={onClick}
        >
            <Group>
                <ThemeIcon color={color} variant="light">
                    {icon}
                </ThemeIcon>

                <Text size="sm">{label}</Text>
            </Group>
        </UnstyledButton>
    )
}
function App() {
    const [view, setView] = useState("customer")

    let mainBody
    switch (view) {
        case "customer":
            mainBody = <CustomerView />
            break
        case "admin":
            mainBody = <AdminView />
            break
        case "delivery":
            mainBody = <DeliveryView />
            break
    }

    return (
        <MantineProvider withGlobalStyles withNormalizeCSS>
            <Header p="xs">
                <Group position="center">
                    <MainLink
                        icon={<IconUser size={16} />}
                        color="green"
                        label="Customer"
                        key="customer"
                        selected={view === "customer"}
                        onClick={() => setView("customer")}
                    />
                    <MainLink
                        icon={<IconCheckupList size={16} />}
                        color="blue"
                        label="Admin"
                        key="admin"
                        selected={view === "admin"}
                        onClick={() => setView("admin")}
                    />
                    <MainLink
                        icon={<IconBoxSeam size={16} />}
                        color="orange"
                        label="Delivery"
                        key="delivery"
                        selected={view === "delivery"}
                        onClick={() => setView("delivery")}
                    />
                </Group>
            </Header>
            <Box
                sx={(theme) => ({
                    background: theme.colors.gray[0],
                    paddingTop: "16px",
                    height: "calc(100vh - 87px)",
                })}
            >
                {mainBody}
            </Box>
        </MantineProvider>
    )
}

export default App
